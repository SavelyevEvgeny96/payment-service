package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderPayloadMapper
import ru.sogaz.site.paymentService.mapper.payment.PaymentMapper
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import java.time.LocalDateTime

@Service
class BuildBatchConsumerServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderPayloadMapper: OrderPayloadMapper,
    private val orderService: OrderService,
    private val paymentMapper: PaymentMapper,
    private val registerPaymentService: RegisterPaymentService,
    private val waitingPaymentDao: WaitingPaymentDao,
    private val orderDao: OrderDao,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val PAYMENT_RECURRENT_SUCCESS = "Платеж успешно сформирован для paymentId: %s"
        private const val PAYMENT_RECURRENT_FALSE = "Платеж не сформирован для paymentId: %s"
    }

    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    override fun upsertBatch(
        batch: List<TaggedPayload>,
        channel: Channel,
    ): List<PaymentRecurrentRegisterData> {
        logger.info(LOG_START.format(batch.size))

        val results =
            batch.map { payload ->
                processSinglePayload(payload.dto) // -> PaymentRecurrentRegisterData
                    .also {
                        // после успешной обработки
                        channel.basicAck(payload.tag, false) // подтверждаем сообщение
                    }
            }
        return results
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun processSinglePayload(payload: OrderPayloadDto): PaymentRecurrentRegisterData {
        // 1) DTO → Request → Order
        val order = buildAndSaveOrder(payload)

        // 2) Order → Payment
        val payment = buildAndSavePayment(order, payload)

        // 3) Регистрация в банке + обновление/логирование
        val registeredPayment = registerAndPersistPayment(payment)

        logger.info("Created payment ${registeredPayment.payment.id} for order ${order.id}")

        // 4) PaymentRecurrentRegisterData
        return registeredPayment
    }

    private fun buildAndSaveOrder(payload: OrderPayloadDto) =
        payload
            .run(orderPayloadMapper::toRequest)
            .run(orderService::makeOrderByRequest)
            .run(orderDao::save)

    private fun buildAndSavePayment(
        order: Order,
        payload: OrderPayloadDto,
    ) = order
        .let { paymentMapper.orderToPayment(it, payload) }
        .run(paymentDao::save)

    private fun registerAndPersistPayment(payment: Payment): PaymentRecurrentRegisterData =
        registerPaymentService
            .registerInBankRecurrent(payment) // -> PaymentRecurrentRegisterData
            .let { registered ->
                val registeredPayment = registered.payment

                if (registeredPayment.paymentBankId.isNullOrBlank()) {
                    // токена нет -> не трогаем paymentStarted, не кладём в waiting, ставим FAIL
                    registeredPayment.state = PaymentStatusEnum.FAIL

                    val saved = paymentDao.save(registeredPayment)

                    logger.error(PAYMENT_RECURRENT_FALSE.format(saved.id))

                    // возвращаем тот же wrapper, но с уже сохранённым payment
                    registered.copy(payment = saved)
                } else {
                    // токен есть -> обычный поток
                    registeredPayment.paymentStarted = LocalDateTime.now()

                    val saved = paymentDao.save(registeredPayment)

                    if (saved.state == PaymentStatusEnum.REG) {
                        logger.info(PAYMENT_RECURRENT_SUCCESS.format(saved.id))
                    } else {
                        logger.error(PAYMENT_RECURRENT_FALSE.format(saved.id))
                    }

                    waitingPaymentDao.saveWaitingForPayment(saved)

                    // снова: обновляем payment внутри wrapper-а
                    registered.copy(payment = saved)
                }
            }
}
