package ru.sogaz.site.paymentService.dto.data

import java.net.URI

data class DataPay(
    val paymentPageUrl: URI,
) : UriKeeper {
    override fun getUri(): URI = paymentPageUrl
}
