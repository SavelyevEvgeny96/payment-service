package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PageInfoRequestParams
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

@RestController
@Tag(name = "Pay", description = "Проведение платежа с редиректом на страницу оплаты")
class PayController(
    private val paymentService: PaymentService,
) {
    @GetMapping("/payment/paySbp/{orderId}")
    fun createPaySbp(
        @PathVariable orderId: String,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        paymentService
            .createSBPPayment(
                orderId = UUID.fromString(orderId),
                payQueryParams = payQueryParams,
            ).wrapToRedirectView()

    @GetMapping("/payment/pay/{orderId}")
    fun pay(
        @PathVariable orderId: String,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        paymentService
            .createCardPayment(
                orderId = UUID.fromString(orderId),
                payQueryParams = payQueryParams,
            ).wrapToRedirectView()

    @GetMapping("/payment/pageinfo/{orderId}")
    fun getInfoPage(
        @PathVariable orderId: UUID,
        requestParams: PageInfoRequestParams,
    ): Response<DataOrderPaymentPageInfo> = paymentService.getOrderPaymentPageInfo(orderId, requestParams)

    private fun DataPay.wrapToRedirectView() = RedirectView(this.paymentPageUrl)
}
