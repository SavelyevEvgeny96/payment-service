package ru.sogaz.site.paymentService.service.rabbit

import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto

interface BuildConsumerService {
    fun processSinglePayload(payload: OrderPayloadDto): PaymentRecurrentRegisterData
}
