package ru.sogaz.site.paymentService.model.v2.web.request

data class PayParams(
    val urlToReturn: String? = null,
    val urlToReturnS: String? = null,
    val urlToReturnF: String? = null,
    val depersonalization: Boolean = false,
    val saveCard: Boolean = false,
)
