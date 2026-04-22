package ru.sogaz.site.paymentService.dto.data

import java.net.URI

data class UrlToReturn(
    private val urlToReturnS: URI? = null,
    private val urlToReturnF: URI? = null,
) {
    fun success(): String? = urlToReturnS?.toString()

    fun failed(): String? = urlToReturnF?.toString()
}
