package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.ResponseStatusPay
import ru.sogaz.siter.models.resonses.Response

interface PaymentStatusCheckerService {
    fun checkUnpaidPayments()

    fun getStatus(
        payment_bank_id: String,
        traceId: String,
    ): Response<ResponseStatusPay>
}
