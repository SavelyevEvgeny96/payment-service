package ru.sogaz.site.paymentService.model.v2.bank.callback

import ru.sogaz.siter.models.resonses.ResponseData

data class GpbSbpReversalCallback(
    val transactionId: String,
)

data class CallbackOkState(
    val state: String = "OK",
) : ResponseData
