package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse

@FeignClient(
    name = "gpb-card-payment-auth-client",
    url = "\${api.gpb.card.basePath}",
)
interface GpbCardPaymentAuthClient {
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
}
