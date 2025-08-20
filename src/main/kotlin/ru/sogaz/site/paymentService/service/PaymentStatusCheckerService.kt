package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.ResponseStatusPay
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.siter.models.resonses.Response

interface PaymentStatusCheckerService {
    fun getStatus(paymentBankId: String): Response<ResponseStatusPay>

    fun checkStatusOrder(
        orderStatus: OrderStatus?,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
    )

    fun processPaymentStatusCheck(payment: Payment)
}
