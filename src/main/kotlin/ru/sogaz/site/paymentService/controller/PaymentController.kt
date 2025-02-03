package ru.sogaz.site.paymentService.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ru.sogaz.site.paymentService.dto.Data
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.siter.models.resonses.Response

/**
 * Контроллер для обработки запросов на создание платежа.
 * Обрабатывает POST запросы для создания ссылки на оплату.
 */
@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService,
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
            @RequestBody @Valid paymentRequest: PaymentRequest): Response<Data> {
            return paymentService.createPayment(paymentRequest,traceId)
        }
}



