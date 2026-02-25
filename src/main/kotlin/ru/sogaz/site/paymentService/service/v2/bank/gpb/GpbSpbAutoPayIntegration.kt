package ru.sogaz.site.paymentService.service.v2.bank.gpb

import ru.sogaz.site.paymentService.model.v2.web.request.pay.GpbSbpAdminAutoPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface GpbSpbAutoPayIntegration {
    fun sbpAdminAutoPay(sbpPayOperationRequest: GpbSbpAdminAutoPayOperationRequest): BankPaymentPageData
}
