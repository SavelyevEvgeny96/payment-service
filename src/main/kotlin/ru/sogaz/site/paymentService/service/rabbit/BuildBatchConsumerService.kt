package ru.sogaz.site.paymentService.service.rabbit

import ru.sogaz.site.paymentService.dto.rabbit.PaymentCreatedEventDto

interface BuildBatchConsumerService {
    fun upsertBatch(batch: List<PaymentCreatedEventDto>)
}
