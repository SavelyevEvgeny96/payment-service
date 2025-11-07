package ru.sogaz.site.paymentService.dto.request

data class PayQueryParams(
    val urlToReturn: String? = null,
    val urlToReturnF: String? = null,
    val depersonalized: Boolean = false,
)
