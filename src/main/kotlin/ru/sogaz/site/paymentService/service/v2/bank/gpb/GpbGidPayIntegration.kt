package ru.sogaz.site.paymentService.service.v2.bank.gpb

import ru.sogaz.site.paymentService.model.v2.web.request.pay.GidPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

/**
 * Интеграция с Газпромбанком для оплаты банковской картой в ГИД.
 */
interface GpbGidPayIntegration {
    /**
     * Регистрирует платежную ссылку в Газпромбанке.
     */
    fun gidPay(gidPayOperationRequest: GidPayOperationRequest): BankPaymentPageData
}
