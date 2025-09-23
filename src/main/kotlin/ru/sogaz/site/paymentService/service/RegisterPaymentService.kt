package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum

interface RegisterPaymentService {
    fun register(
        order: Order,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn = UrlToReturn(),
    ): Payment
}
