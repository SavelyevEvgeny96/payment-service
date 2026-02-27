package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.config.FeignTimingConfig
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.bank.GPBRefundResponseDto
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.dto.response.bank.SessionIdDtoResponse

/**
 * Feign-клиент для интеграции с API Газпромбанка (Card Payment).
 *
 * Используется для:
 * - управления сессией взаимодействия с GPБ
 * - инициации платежей и возвратов
 * - получения статусов платежей
 */
@FeignClient(
    name = "gpb-card-payment-client",
    url = "\${api.gpb.card.basePath}",
    configuration = [FeignTimingConfig::class]
)
interface GpbCardPaymentClient {
    /**
     * Получает токен для инициации платежной операции.
     *
     * @param portalId идентификатор портала в системе ГПБ
     * @return токен платежа
     */
    @PostMapping(
        value = ["/{portalId}/token"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getToken(
        @PathVariable portalId: String,
    ): GazpromTokenResponse

    /**
     * Открывает сессию взаимодействия с API ГПБ.
     *
     * Полученный sessionId должен использоваться в заголовке
     * `X-IV-Authorization` при выполнении последующих запросов
     * и обязательно закрываться вызовом {@link #finishSessionId}.
     *
     * @param portalId идентификатор портала в системе ГПБ
     * @param identifier идентификатор клиента (логин)
     * @param password пароль клиента
     * @return объект с идентификатором открытой сессии
     */
    @PostMapping(
        value = ["/{portalId}/session/start"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun getSessionId(
        @PathVariable portalId: String,
        @RequestParam("identifier") identifier: String,
        @RequestParam("password") password: String,
    ): SessionIdDtoResponse

    /**
     * Завершает ранее открытую сессию взаимодействия с API ГПБ.
     *
     * Должен вызываться после выполнения всех операций в рамках сессии,
     * даже в случае возникновения ошибок.
     *
     * @param sessionHeader значение заголовка `X-IV-Authorization`
     * @param portalId идентификатор портала в системе ГПБ
     */
    @PostMapping(
        value = ["/{portalId}/session/finish"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun finishSessionId(
        @RequestHeader("X-IV-Authorization")
        sessionHeader: String,
        @PathVariable portalId: String,
    )

    /**
     * Инициирует возврат средств по ранее выполненному платежу.
     *
     * Требует предварительно открытую сессию.
     *
     * @param portalId идентификатор портала в системе ГПБ
     * @param paymentBankId идентификатор платежа в системе банка
     * @param sessionHeader значение заголовка `X-IV-Authorization`
     * @param amount сумма возврата
     * @param currency валюта операции (например, RUB)
     * @param comment комментарий к операции возврата
     *
     * @return результат операции возврата средств
     */
    @PostMapping(
        value = ["/{portalId}/merchant/history/trx/{paymentBankId}/refund"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun startRefund(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String?,
        @RequestHeader("X-IV-Authorization")
        sessionHeader: String,
        @RequestParam("amount") amount: String,
        @RequestParam("currency") currency: String,
        @RequestParam("comment") comment: String,
    ): GPBRefundResponseDto

    /**
     * Инициирует разовый платеж.
     *
     * @param portalId идентификатор портала в системе ГПБ
     * @param token токен платежа
     * @param request тело запроса с параметрами платежа
     *
     * @return ответ ГПБ с результатом инициации платежа
     */
    @PostMapping(
        value = ["/{portalId}/payment/{token}/start"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun startPayment(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GPBPaymentRequest,
    ): GazpromCardPaymentResponse

    /**
     * Инициирует рекуррентный платеж (регистрация карты).
     *
     * @param portalId идентификатор портала в системе ГПБ
     * @param token токен платежа
     * @param request тело запроса с параметрами платежа
     *
     * @return ответ с результатом регистрации карты
     */
    @PostMapping(
        value = ["/{portalId}/payment/{token}/start"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun startPaymentRecurrent(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GPBPaymentRequest,
    ): RegisterCardResponseDto

    /**
     * Получает текущий статус платежа.
     *
     * @param portalId идентификатор портала в системе ГПБ
     * @param paymentBankId идентификатор платежа в системе банка
     *
     * @return статус платежа
     */
    @GetMapping(value = ["/{portalId}/payment/{paymentBankId}"])
    fun getPaymentStatus(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String,
    ): GpbCardPaymentStatusResponse
}
