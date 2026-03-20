package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.dto.response.bank.SessionIdDtoResponse
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbRefundParams
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbRefundCardPayResponse

@FeignClient(
    name = "gpb-refund-card-client",
    url = "\${api.gpb.card.basePath}",
)
interface GpbCardRefundClient {
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
    fun startSession(
        @PathVariable portalId: String,
        @RequestParam identifier: String,
        @RequestParam password: String,
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
    fun finishSession(
        @RequestHeader("X-IV-Authorization") sessionHeader: String,
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
    fun refund(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String?,
        @RequestHeader("X-IV-Authorization") sessionHeader: String,
        @SpringQueryMap gpbRefundParams: GpbRefundParams,
    ): GpbRefundCardPayResponse
}
