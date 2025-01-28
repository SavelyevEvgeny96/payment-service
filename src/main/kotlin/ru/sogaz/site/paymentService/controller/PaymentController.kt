package ru.sogaz.site.paymentService.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentResponse
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.TokenServiceImpl

/**
 * Контроллер для обработки запросов на создание платежа.
 * Обрабатывает POST запросы для создания ссылки на оплату.
 */
@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService,
    private val tokenUtil: TokenServiceImpl
) {

        /**
         * Метод для создания платежа.
         * Принимает авторизационный токен, TraceId и данные о платеже.
         * Валидирует входящие данные и передает их в сервис для дальнейшей обработки.
         * @param authorization Токен авторизации в формате "Bearer <token>"
         * @param traceId Идентификатор трассировки
         * @param paymentRequest Объект данных о платеже
         * @return Ответ с кодом состояния и данными о платеже или ошибкой
         */

        @PostMapping("/create")
        fun createPayment(
            @RequestHeader("Authorization") authorization: String,
            @RequestHeader("TraceId") traceId: String,
            @RequestBody @Valid paymentRequest: PaymentRequest, result: BindingResult): PaymentResponse {
            // Если есть ошибки валидации
            if (result.hasErrors()) {
                val errors = result.allErrors.map { it.defaultMessage }
                return ResponseEntity<PaymentResponse>()
            }


            return paymentService.createPayment(paymentRequest,traceId)
        }
}



