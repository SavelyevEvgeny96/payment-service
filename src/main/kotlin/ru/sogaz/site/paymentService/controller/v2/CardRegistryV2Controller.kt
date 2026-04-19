package ru.sogaz.site.paymentService.controller.v2

import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.CardRegistryV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayRegOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService
import ru.sogaz.siter.models.resonses.Response

@RestController
class CardRegistryV2Controller(
    private val payOperationService: PayOperationService,
) : WrapResponseController(),
    CardRegistryV2Api {
    override fun cardRegistry(request: PayRegOperationRequest): Response<BankPaymentPageData> =
        request
            .run(payOperationService::regPayOperation)
            .wrapToSuccessResponse(200)
}
