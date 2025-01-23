package ru.sogaz.site.paymentService.service

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.config.ApiConfig
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.validation.PaymentValidator

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
@Service
class PaymentService(
    private val paymentValidator: PaymentValidator,
    private val paymentRepository: PaymentRepository,
    private val apiConfig: ApiConfig
) {
    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param paymentRequest Данные о платеже
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект Payment, содержащий информацию о платежном запросе
     */
    fun createPayment(paymentRequest: PaymentRequest) : PaymentResponse {
        validatePaymentData(paymentRequest)


        val savedPayment = paymentRepository.save(payment)

        // Генерация ссылки на оплату
        val paymentUrl = "${apiConfig.paymentUrl}${savedPayment.id}"

        // Возвращаем DTO с ответом
        return PaymentResponse(
            status = "success",
            code = savedPayment.id.toString(),
            url = paymentUrl
        )
    }

    /**
     * Метод для валидации данных платежа.
     * Проверяет все обязательные поля на корректность.
     * @param paymentRequest Данные о платеже
     * @throws Exception Если какие-либо поля невалидны
     */
    private fun validatePaymentData(paymentRequest: PaymentRequest) {
        paymentValidator.validateEmail(paymentRequest.recipientEmail)
        paymentValidator.validatePhone(paymentRequest.recipientPhone)
        paymentValidator.validatePolicyholder(paymentRequest.policyholder)
        paymentValidator.validateExternalSystemCode(paymentRequest.externalSystemCode)
        paymentValidator.validatePaymentEndDate(paymentRequest.paymentEndDate)
        paymentValidator.validateDateFormat(paymentRequest.paymentEndDate)
        paymentValidator.validateBank(paymentRequest.bank)
    }
}
