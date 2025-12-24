package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.PayV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.model.web.request.PayRequest
import ru.sogaz.site.paymentService.service.PaymentService

@RestController
@Tag(name = "Pay v2", description = "Проведение платежа с редиректом на страницу оплаты")
class PayV2Controller(
    private val paymentService: PaymentService,
) : WrapResponseController(),
    PayV2Api {
    override fun paySbp(
        payQueryParams: PayQueryParams,
        payRequest: PayRequest,
    ): DataPay {
        TODO("Not yet implemented")
    }

    override fun pay(
        payQueryParams: PayQueryParams,
        payRequest: PayRequest,
    ): DataPay {
        TODO("Not yet implemented")
    }
}
