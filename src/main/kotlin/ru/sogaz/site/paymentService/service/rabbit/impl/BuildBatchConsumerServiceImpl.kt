package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
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
) : BuildBatchConsumerService {

    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val PAYMENT_RECURRENT_SUCCESS = "Платеж успешно сформирован для paymentId: %d "
        private const val PAYMENT_RECURRENT_FALSE = "Платеж не сформирован для paymentId: %d  "
    }

    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    override fun upsertBatch(batch: List<OrderPayloadDto>) {
        logger.info(LOG_START.format(batch.size))
        batch.forEach { payload ->
            processSinglePayload(payload)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun processSinglePayload(payload: OrderPayloadDto) {
        // 1) DTO → Request → Order
        val order = buildAndSaveOrder(payload)

        // 2) Order → Payment
        val payment = buildAndSavePayment(order, payload)

        // 3) Регистрация в банке + обновление/логирование
        val registeredPayment = registerAndPersistPayment(payment)

        logger.info("Created payment ${registeredPayment.id} for order ${order.id}")
    }

    private fun buildAndSaveOrder(payload: OrderPayloadDto) =
        payload
            .run(orderPayloadMapper::toRequest)
            .run(orderService::saveEntityFromRequest)

    private fun buildAndSavePayment(order: Order, payload: OrderPayloadDto) =
        order
            .let { paymentMapper.orderToPayment(it, payload) }
            .run(paymentDao::save)

    private fun registerAndPersistPayment(payment: Payment): Payment =
        registerPaymentService
            .registerInBank(payment, null, true)
            .apply {
                paymentStarted = LocalDateTime.now()
            }
            .run(paymentDao::save)
            .also(waitingPaymentDao::saveWaitingForPayment)
            .also { paymentUpdate ->
                if (paymentUpdate.state == PaymentStatusEnum.REG) {
                    logger.info(PAYMENT_RECURRENT_SUCCESS.format(paymentUpdate.id))
                } else {
                    logger.error(
                        PAYMENT_RECURRENT_FALSE.format(paymentUpdate.id)
                    )
                }
            }
}
