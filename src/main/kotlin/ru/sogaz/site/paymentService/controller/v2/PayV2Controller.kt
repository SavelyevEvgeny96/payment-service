package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.PayV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService
import ru.sogaz.siter.models.resonses.Response

@RestController
@Tag(name = "Pay v2", description = "Проведение платежа с редиректом на страницу оплаты")
class PayV2Controller(
    private val payOperationService: PayOperationService,
) : WrapResponseController(),
    PayV2Api {
    companion object {
        private const val CARD_PAY_SUCCESS_CODE = 1101510200
        private const val SBP_PAY_SUCCESS_CODE = 1
    }

    override fun pay(cardPayOperationRequest: CardPayOperationRequest): Response<BankPaymentPageData> =
        cardPayOperationRequest
            .run(payOperationService::cardPayOperation)
            .wrapToSuccessResponse(CARD_PAY_SUCCESS_CODE)

    override fun paySbp(sbpPayOperationRequest: SbpPayOperationRequest): Response<BankPaymentPageData> =
        sbpPayOperationRequest
            .run(payOperationService::sbpPayOperation)
            .wrapToSuccessResponse(SBP_PAY_SUCCESS_CODE)

    override fun recurrentCardPay(cardRecurrentOperationRequest: CardRecurrentOperationRequest): Response<BankOperationDetails> =
        cardRecurrentOperationRequest
            .run(payOperationService::recurrentOperation)
            .wrapToSuccessResponse(0)
}
