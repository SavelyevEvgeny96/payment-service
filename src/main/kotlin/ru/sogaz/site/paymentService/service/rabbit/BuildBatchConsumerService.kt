package ru.sogaz.site.paymentService.service.rabbit

import com.rabbitmq.client.Channel
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto

interface BuildBatchConsumerService {
    fun upsertBatch(
        batch: List<TaggedPayload<OrderPayloadDto>>,
        channel: Channel,
    ): List<PaymentRecurrentRegisterData>
}
