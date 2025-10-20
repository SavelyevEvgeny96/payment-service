package ru.sogaz.site.paymentService.dto.data

data class UrlToReturn(
    private val urlToReturnS: String? = null,
    private val urlToReturnF: String? = null,
) {
    fun success(): String? = urlToReturnS

    fun failed(): String? = urlToReturnF
}
