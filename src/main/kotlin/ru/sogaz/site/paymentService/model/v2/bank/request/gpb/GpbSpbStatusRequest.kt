package ru.sogaz.site.paymentService.model.v2.bank.request.gpb

data class GpbSpbStatusRequest(
    val qrcIds: List<String> = emptyList(),
) {
    constructor(id: String) : this(listOf(id))
}
