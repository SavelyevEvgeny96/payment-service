package ru.sogaz.site.paymentService.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.dto.DataOrder
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
     * Метод для создания заявки.
     * @param traceId Идентификатор трассировки
     * @return Ответ с кодом состояния и данными о платеже или ошибкой
     */

    @PostMapping("/create")
    fun createOrder(
        @RequestHeader("TraceId") traceId: String,
        @RequestBody requestWrapper: PaymentRequestWrapper,
    ): ResponseEntity<Response<DataOrder>> {
        requestWrapper.payments.forEach { paymentRequest ->
            paymentRequest.traceId = traceId
            paymentRequestValidator.isValid(paymentRequest, requestWrapper)
        }
        return paymentService.createOrder(requestWrapper, traceId)
    }
    /**
     * Метод для создания платежа.
     * @param traceId Идентификатор трассировки
     * @return Ответ с кодом состояния и данными о платеже или ошибкой
     */
//    @PostMapping("/pay")
//    fun createPay(
//        @RequestHeader("TraceId") traceId: String,
//        @RequestBody paymentPayRequest: PaymentPayRequest
//    ): ResponseEntity<Response<DataPay>> {
//        paymentPayRequest.traceId = traceId
//        return paymentService.createPayment(
//            paymentPayRequest, traceId
//        )
//    }
}
