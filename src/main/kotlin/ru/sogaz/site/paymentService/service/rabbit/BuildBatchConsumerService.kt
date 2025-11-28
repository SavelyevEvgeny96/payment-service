package ru.sogaz.site.paymentService.service.rabbit

import com.rabbitmq.client.Channel
import ru.sogaz.site.paymentService.dto.data.BatchRecurrentResult
import ru.sogaz.site.paymentService.dto.data.TaggedPayload

interface BuildBatchConsumerService {
    fun upsertBatch(
        batch: List<TaggedPayload>,
        channel: Channel,
    ): BatchRecurrentResult
}
