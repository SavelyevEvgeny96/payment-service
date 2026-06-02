package ru.sogaz.site.paymentService.dto.request

data class GPBStatusSBPRequest(
    val qrcIds: List<String> = emptyList(),
) {
    constructor(id: String) : this(listOf(id))
}
