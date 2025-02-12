package ru.sogaz.site.paymentService.dto

import jakarta.validation.constraints.NotNull

data class PaymentRequestWrapper(
    @NotNull
    val payments: List<PaymentRequest>,
    val urlToReturn: String?,
    val urlToDecline: String?,
    val customURL: String?,
    val bank: String?,
    @NotNull
    val paymentEndDate: String,
    @NotNull
    val recipientEmail: String,
    @NotNull
    val recipientPhone: String?,
    val policyHolder: String?,
    val policyHolderDoc: String?,
    val recipientUserId: String?,
    val needReceipt: Boolean?,
)
