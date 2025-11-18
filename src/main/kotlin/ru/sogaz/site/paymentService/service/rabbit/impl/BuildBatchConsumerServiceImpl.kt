package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderPayloadMapper
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService

@Service
class BuildBatchConsumerServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderPayloadMapper: OrderPayloadMapper,
    private val orderService: OrderService,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
    }

    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>) {
//        Вернем когда будет перенесен функционал в сервис полноценно пока обработка другая
//        val nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString()
//        val payments = batch.map(paymentMapper::toPayment)
//        val listPaymentId = paymentDao.batchInsertPayment(payments)
        batch
            .map(orderPayloadMapper::toRequest)
            .forEach(orderService::saveEntityFromRequest)

        logger.info(LOG_START.format(batch.size))
//        logger.info("listPaymentId: $listPaymentId")
    }
}
