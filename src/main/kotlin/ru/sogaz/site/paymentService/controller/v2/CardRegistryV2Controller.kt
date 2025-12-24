package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.CardRegistryV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.service.AuthorizationService
import ru.sogaz.site.paymentService.service.CardRegistryService

@RestController
@Tag(name = "Card Registration v2", description = "Регистрация карт")
class CardRegistryV2Controller(
    private val cardRegistryService: CardRegistryService,
    private val authorizationService: AuthorizationService,
) : WrapResponseController(),
    CardRegistryV2Api {
    override fun cardRegistry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        token: String,
    ): DataPay {
        TODO("Not yet implemented")
    }
}
