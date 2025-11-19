package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
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
    private val registerPaymentService: RegisterPaymentService
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
    }

    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>) {
        logger.info(LOG_START.format(batch.size))

        batch.forEach { payload ->

            // 1) Превращаем DTO → Request
            val orderRequest = orderPayloadMapper.toRequest(payload)

            // 2) Сохраняем Order и получаем сохранённую сущность
            val order = orderService.saveEntityFromRequest(orderRequest)

            // 3) Превращаем Order → Payment (маппер MapStruct)
            val payment = paymentMapper.orderToPayment(order, payload)

            // 4) Сохраняем Payment
            paymentDao.save(payment)
            registerPaymentService.registerInBank(payment, null, true)

            logger.info("Created payment ${payment.id} for order ${order.id}")
        }
    }
}
