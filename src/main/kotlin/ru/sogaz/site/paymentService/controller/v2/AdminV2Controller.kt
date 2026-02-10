package ru.sogaz.site.paymentService.controller.v2

import ru.sogaz.site.paymentService.api.doc.v2.AdminV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.model.v2.web.request.PayRequest
import ru.sogaz.site.paymentService.service.PaymentService

class AdminV2Controller(
    private val paymentService: PaymentService,
) : WrapResponseController(),
    AdminV2Api {
    override fun createPaySbp(
        processPayments: String?,
        paymentDelay: String?,
        paymentStatus: String?,
        payQueryParams: PayQueryParams,
        payRequest: PayRequest,
    ): DataPay {
        TODO("Not yet implemented")
    }
}
