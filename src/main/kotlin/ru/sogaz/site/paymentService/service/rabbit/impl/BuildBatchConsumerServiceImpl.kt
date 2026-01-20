package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderPayloadMapper
import ru.sogaz.site.paymentService.mapper.payment.PaymentMapper
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService

@Service
class BuildBatchConsumerServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderPayloadMapper: OrderPayloadMapper,
    private val orderService: OrderService,
    private val paymentMapper: PaymentMapper,
    private val registerPaymentService: RegisterPaymentService,
    private val orderDao: OrderDao,
) : BuildBatchConsumerService {
    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    override fun upsertBatch(
        batch: List<TaggedPayload<OrderPayloadDto>>,
        channel: Channel,
    ): List<PaymentRecurrentRegisterData> {
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
        val registeredPayment =
            registerPaymentService
                .registerInBankRecurrent(payment)

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
}
