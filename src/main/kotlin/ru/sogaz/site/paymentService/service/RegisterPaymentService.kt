package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum

interface RegisterPaymentService {
    fun register(
        order: Order,
        paymentTypeEnum: PaymentTypeEnum,
        payQueryParams: PayQueryParams = PayQueryParams(),
        headersParams: GpbSbpHeadersParams? = null,
    ): Payment

    fun registerInBank(
        payment: Payment,
        headersParams: GpbSbpHeadersParams? = null,
        recurrent: Boolean = false,
    ): Payment
}
