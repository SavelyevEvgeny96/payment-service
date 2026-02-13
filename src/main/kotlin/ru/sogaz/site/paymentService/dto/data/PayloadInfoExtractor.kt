package ru.sogaz.site.paymentService.dto.data

fun interface PayloadInfoExtractor {
    fun extract(body: String): PayloadInfo?
}
