package ru.sogaz.site.paymentService.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.stereotype.Component
import ru.sogaz.siter.models.resonses.Response

/**
 * Описание эндпоинтов контроллера платежей для Swagger.
 */
@Component
class PaymentControllerDocs {
    @Operation(
        summary = "Создание платежа",
        description = "Создает запись о платеже и возвращает ссылку на оплату",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешное создание платежа",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Response::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Ошибка авторизации",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации входных данных",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun createPayment() {}
}
