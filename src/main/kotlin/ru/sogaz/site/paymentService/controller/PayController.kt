package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.api.doc.v1.PayV1Api
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.service.PaymentService
import java.util.UUID

@RestController
@Tag(name = "Pay", description = "Проведение платежа с редиректом на страницу оплаты")
class PayController(
    private val paymentService: PaymentService,
) : WrapResponseController(),
    PayV1Api {
    override fun createPaySbp(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        paymentService
            .createSBPPayment(
                orderId = orderId,
                payQueryParams = payQueryParams,
            ).wrapToRedirectView()

    override fun pay(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        paymentService
            .createCardPayment(
                orderId = orderId,
                payQueryParams = payQueryParams,
            ).wrapToRedirectView()
}
