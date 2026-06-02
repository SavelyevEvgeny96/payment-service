package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.Payment

interface TokenService {
    fun exchangeForToken(depersonalization: Boolean): String?

    fun saveToken(payment: Payment): String

    fun takePortalId(depersonalization: Boolean): String

    fun takeMerchantId(depersonalization: Boolean): String
}
