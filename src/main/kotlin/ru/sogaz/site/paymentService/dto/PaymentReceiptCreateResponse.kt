package ru.sogaz.site.paymentService.dto

import ru.sogaz.siter.models.resonses.ValidationErrorData

data class PaymentReceiptCreateResponse(
    val status: String,
    val code: Int,
    var traceId: String,
    val innerError: String? = null,
    val messagesError: String? = null,
    val responseUuid: String,
    var errorsValidate: List<ValidationErrorData?>? = null,
    val data: PaymentData,
)

data class PaymentData(
    val state: String,
    val externalId: String,
)
