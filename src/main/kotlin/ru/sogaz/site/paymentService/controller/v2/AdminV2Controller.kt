package ru.sogaz.site.paymentService.controller.v2

import ru.sogaz.site.paymentService.api.doc.v2.AdminV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.operation.impl.OperationServiceImpl

class AdminV2Controller(
    private val operationServiceImpl: OperationServiceImpl,
) : WrapResponseController(),
    AdminV2Api {
    override fun createPaySbp(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData {
        TODO("Not yet implemented")
    }
}
