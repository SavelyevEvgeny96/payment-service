package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.service.AuthorizationService
import ru.sogaz.site.paymentService.service.CardRegistryService

@RestController
@Tag(name = "Order", description = "Управление заказами")
@Validated
class CardRegistryController(
    private val cardRegistryService: CardRegistryService,
    private val authorizationService: AuthorizationService,
) : WrapResponseController() {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрация банковской карты",
    )
    @Parameters(
        Parameter(name = "unifiedId", description = "Идентификатор единого профиля клиента", required = true),
        Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты", example = "http://www.sogaz.ru"),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты"),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты"),
        Parameter(name = "depersonalization", description = "Флаг необходимости анонимизированной оплаты", example = "true"),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @GetMapping("/payment/users/{unifiedId}/card")
    fun cardRegistry(
        @PathVariable unifiedId: String,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) token: String,
    ): RedirectView {
        // проверка JWT токена: в gwt
        // проверка прав доступа. Из JWT проверить Payload.clientId в расшифровке токена с external_system_code в таблице "Системы клиенты" (client_systems).
        val clientSystem: ClientSystem = authorizationService.checkPermissionByClientId(token)
        return cardRegistryService
            .registry(
                unifiedId = unifiedId,
                payQueryParams = payQueryParams,
                clientId = clientSystem.externalSystemCode,
            ).wrapToRedirectView()
    }
}
