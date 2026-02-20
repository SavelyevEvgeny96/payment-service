package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.PayV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.impl.OperationServiceImpl

@RestController
@Tag(name = "Pay v2", description = "Проведение платежа с редиректом на страницу оплаты")
class PayV2Controller(
    private val operationServiceImpl: OperationServiceImpl,
) : WrapResponseController(),
    PayV2Api {
    override fun pay(cardPayOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        operationServiceImpl.pay(cardPayOperationRequest)

    override fun paySbp(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData =
        TODO("Not yet implemented")
}
