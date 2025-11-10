package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.validation.PermissionValidator
import ru.sogaz.siter.models.resonses.Response

@RestController
@Tag(name = "Order", description = "Управление заказами")
class OrderController(
    private val orderService: OrderService,
    private val permissionValidator: PermissionValidator,
) {
    /**
     * Метод для создания заявки.
     * getTraceId() Идентификатор трассировки
     * @return Ответ с кодом состояния и данными о платеже или ошибкой
     */
    @Operation(
        summary = "Создать заявку на оплату",
        description = "Создает заявку и возвращает ссылку на оплату.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешное создание платежа",
                content = [Content(schema = Schema(implementation = DataOrder::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Неавторизованный запрос",
                content = [
                    Content(
                        schema =
                            Schema(
                                example =
                                    "{\"status\": \"error\", \"code\": -1101500401," +
                                        " \"messageError\": \"Ваш запрос не авторизован\"}",
                            ),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен",
                content = [
                    Content(
                        schema =
                            Schema(
                                example =
                                    "{\"status\": \"error\", \"code\": -1101500403, " +
                                        "\"messageError\": \"Вам запрещен доступ к запрашиваемому ресурсу\"}",
                            ),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Ошибка валидации данных",
                content = [
                    Content(
                        schema =
                            Schema(
                                example =
                                    "{\"status\": \"error\", \"code\": -1101500422, " +
                                        "\"messageError\": \"Не все обязательные данные указаны корректно\"}",
                            ),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("payment/create")
    fun createOrder(
        @Valid @RequestBody requestWrapper: OrderRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): ResponseEntity<Response<DataOrder>> {
        requestWrapper.clientId = permissionValidator.checkPermission(authorization)?.externalSystemCode
        return ResponseEntity.ok(orderService.createOrder(requestWrapper))
    }

    @GetMapping("payment/order/status/{orderId}")
    fun getOrderStatus(
        @PathVariable orderId: String,
    ): Response<DataGetOrderStatus> = orderService.getOrderStatus(orderId)
}
