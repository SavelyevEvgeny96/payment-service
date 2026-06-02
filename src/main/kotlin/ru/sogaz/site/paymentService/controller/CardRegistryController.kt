package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.dto.request.PayQueryParamsWithRequiredFields
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.service.AuthorizationService
import ru.sogaz.site.paymentService.service.CardRegistryService

@RestController
@Tag(name = "Card registration", description = "Регистрация карт")
@Validated
class CardRegistryController(
    private val cardRegistryService: CardRegistryService,
    private val authorizationService: AuthorizationService,
) : WrapResponseController() {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Инициирует регистрацию банковской карты и выполняет редирект на платежную страницу",
    )
    @ApiResponse(
        responseCode = "302",
        description = "Редирект на страницу оплаты",
    )
    @GetMapping("/payment/users/{unifiedId}/card")
    fun cardRegistry(
        @PathVariable
        @Parameter(
            description = "Идентификатор единого профиля клиента",
            required = true,
            example = "123456789",
        )
        unifiedId: String,
        @Valid
        payQueryParams: PayQueryParamsWithRequiredFields,
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(
            description = "Bearer токен авторизации",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        )
        token: String,
    ): RedirectView {
        val clientSystem: ClientSystem = authorizationService.checkPermissionByClientId(token)

        return cardRegistryService
            .registry(
                unifiedId = unifiedId,
                payQueryParams = payQueryParams,
                clientId = clientSystem.externalSystemCode,
            ).wrapToRedirectView()
    }
}
