package ru.sogaz.site.paymentService.model.v2.bank.callback

data class GpbSbpReversalCallback(
    val transactionId: String,
)

data class CallbackOkState(
    val state: String = "OK",
)
