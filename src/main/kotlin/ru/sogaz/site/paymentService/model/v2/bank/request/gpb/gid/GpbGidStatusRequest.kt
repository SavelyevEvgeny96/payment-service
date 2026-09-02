package ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid

data class GpbGidStatusRequest(
    val pgaTrxId: String,
    val portalId: String,
)
