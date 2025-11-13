package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dto.rabbit.PaymentCreatedEventDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.PaymentMapper
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class BuildBatchConsumerServiceImpl(
    private val paymentMapper: PaymentMapper,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
    }

    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<PaymentCreatedEventDto>) {
        val nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString()
        val payments = batch.map(paymentMapper::toPayment)
        logger.info(LOG_START.format(batch.size))
        logger.info("payments: $payments")
    }
}
