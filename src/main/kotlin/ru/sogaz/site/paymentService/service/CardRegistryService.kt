package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams

interface CardRegistryService {
    fun registry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): DataPay
}
