package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.BatchRecurrentResult
import ru.sogaz.site.paymentService.dto.data.SinglePaymentResult
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
import java.util.UUID

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
    ): BatchRecurrentResult {
        logger.info(LOG_START.format(batch.size))

        val paymentsResult = mutableListOf<UUID>()

        batch.forEach { payload ->
            val result = processSinglePayload(payload.dto)
            val orderIdRecurrent = result.orderIdRecurrent
            if (orderIdRecurrent != null) {
                if (result.status == PaymentStatusEnum.REG) {
                    paymentsResult += orderIdRecurrent
                }
            } else {
                logger.warn(
                    "orderIdRecurrent is null for payload orderIdRecurrent=${payload.dto.orderIdRecurrent} " +
                        "status=${result.status}",
                )
            }
            // 4) ACK за успешно распарсенное сообщение
            channel.basicAck(payload.tag, false)
        }

        return BatchRecurrentResult(
            paymentsResult = paymentsResult,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun processSinglePayload(payload: OrderPayloadDto): SinglePaymentResult {
        // 1) DTO → Request → Order
        val order = buildAndSaveOrder(payload)

        // 2) Order → Payment
        val payment = buildAndSavePayment(order, payload)

        // 3) Регистрация в банке + обновление/логирование
        val registeredPayment = registerAndPersistPayment(payment)

        logger.info("Created payment ${registeredPayment.id} for order ${order.id}")

        // 4) Возвращаем status + orderIdRecurrent
        return SinglePaymentResult(
            orderIdRecurrent = registeredPayment.order.orderIdRecurrent,
            status = registeredPayment.state,
        )
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

    private fun registerAndPersistPayment(payment: Payment): Payment =
        registerPaymentService
            .registerInBank(payment, null, true)
            .let { registered ->
                if (registered.paymentBankId.isNullOrBlank()) {
                    // токена нет -> не трогаем paymentStarted, не кладём в waiting, ставим FAIL
                    registered
                        .apply { state = PaymentStatusEnum.FAIL }
                        .run(paymentDao::save)
                        .also { saved ->
                            logger.error(PAYMENT_RECURRENT_FALSE.format(saved.id))
                        }
                } else {
                    // токен есть -> обычный поток
                    registered
                        .apply { paymentStarted = LocalDateTime.now() }
                        .run(paymentDao::save)
                        .also { paymentUpdate ->
                            if (paymentUpdate.state == PaymentStatusEnum.REG) {
                                logger.info(PAYMENT_RECURRENT_SUCCESS.format(paymentUpdate.id))
                            } else {
                                logger.error(PAYMENT_RECURRENT_FALSE.format(paymentUpdate.id))
                            }
                        }.also(waitingPaymentDao::saveWaitingForPayment)
                }
            }
}
