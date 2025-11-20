package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.BatchRecurrentResult
import ru.sogaz.site.paymentService.dto.data.SinglePaymentResult
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderPayloadMapper
import ru.sogaz.site.paymentService.mapper.payment.PaymentMapper
import ru.sogaz.site.paymentService.properties.RabbitListenerProps
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.site.paymentService.service.payment.PaymentStatusServiceImpl.Companion.START_LOG_MESSAGE_QUEUE
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
@Service
class BuildBatchConsumerServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderPayloadMapper: OrderPayloadMapper,
    private val orderService: OrderService,
    private val paymentMapper: PaymentMapper,
    private val registerPaymentService: RegisterPaymentService,
    private val waitingPaymentDao: WaitingPaymentDao

) : BuildBatchConsumerService {

    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val PAYMENT_RECURRENT_SUCCESS = "Платеж успешно сформирован для paymentId: %d"
        private const val PAYMENT_RECURRENT_FALSE = "Платеж не сформирован для paymentId: %d"
    }

    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    override fun upsertBatch(batch: List<OrderPayloadDto>): BatchRecurrentResult {
        logger.info(LOG_START.format(batch.size))

        val paid = mutableListOf<UUID>()
        val unpaid = mutableListOf<UUID>()

        batch.forEach { payload ->

                val result = processSinglePayload(payload)

                val orderIdRecurrent = result.orderIdRecurrent
                if (orderIdRecurrent != null) {
                    if (result.status == PaymentStatusEnum.REG) {
                        paid += orderIdRecurrent
                    } else {
                        unpaid += orderIdRecurrent
                    }
                } else {
                    logger.warn(
                        "orderIdRecurrent is null for payload orderIdRecurrent=${payload.orderIdRecurrent} " +
                                "status=${result.status}"
                    )
                }
        }

        return BatchRecurrentResult(
            paid = paid,
            unpaid = unpaid
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
            orderIdRecurrent = registeredPayment.order?.orderIdRecurrent,
            status = registeredPayment.state
        )
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
