package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.api.doc.v1.AdminV1Api
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.service.PaymentService
import java.util.UUID

@RestController
@Profile(value = ["test", "local"])
@Tag(name = "Admin", description = "Административный контроллер доступный только на тесте")
class AdminController(
    private val paymentService: PaymentService,
) : WrapResponseController(),
    AdminV1Api {
    override fun createPaySbp(
        @PathVariable orderId: String,
        payQueryParams: PayQueryParams,
        @RequestHeader("processPayments") processPayments: String?,
        @RequestHeader("paymentDelay") paymentDelay: String?,
        @RequestHeader("paymentStatus") paymentStatus: String?,
    ): RedirectView =
        paymentService
            .createSBPPayment(
                orderId = UUID.fromString(orderId),
                payQueryParams = payQueryParams,
                paymentDelay = paymentDelay,
                processPayments = processPayments,
                paymentStatus = paymentStatus,
            ).wrapToRedirectView()
}
