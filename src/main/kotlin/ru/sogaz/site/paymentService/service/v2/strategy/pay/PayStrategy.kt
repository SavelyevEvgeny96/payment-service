package ru.sogaz.site.paymentService.service.v2.strategy.pay

import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface PayStrategy {
    fun execute(): BankPaymentPageData
}
