package ru.sogaz.site.paymentService.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.dto.Data
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.validation.PaymentRequestValidator
import ru.sogaz.siter.models.resonses.Response

/**
 * Контроллер для обработки запросов на создание платежа.
 * Обрабатывает POST запросы для создания ссылки на оплату.
 */
@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService,
    private val paymentRequestValidator: PaymentRequestValidator,
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
        @RequestHeader("TraceId") traceId: String,
        @RequestBody requestWrapper: PaymentRequestWrapper,
    )
    : ResponseEntity<Response<Data>> {
        requestWrapper.payments.forEach { paymentRequest ->
            paymentRequest.traceId = traceId
            paymentRequestValidator.isValid(paymentRequest,requestWrapper)
        }
        return paymentService.createPayment(requestWrapper, traceId)
    }



}
