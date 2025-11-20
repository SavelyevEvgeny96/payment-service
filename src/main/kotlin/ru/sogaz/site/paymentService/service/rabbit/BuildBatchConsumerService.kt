package ru.sogaz.site.paymentService.service.rabbit

import ru.sogaz.site.paymentService.dto.data.BatchRecurrentResult
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto

interface BuildBatchConsumerService {
    fun upsertBatch(batch: List<OrderPayloadDto>): BatchRecurrentResult
}
