package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.clients.gpb.GpbGidClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbGidMapper
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GidPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbGidProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbGidPayIntegration

/**
 * Сервис интеграции с Газпромбанком для первого шага оплаты банковской картой в ГИД.
 */
@Service
class GpbGidIntegrationImpl(
    private val gpbGidClient: GpbGidClient,
    private val gpbGidProperties: GpbGidProperties,
    private val gpbGidMapper: GpbGidMapper,
) : GpbGidPayIntegration {
    override fun gidPay(gidPayOperationRequest: GidPayOperationRequest): BankPaymentPageData =
        gpbGidMapper
            .toGpbGidPayRequest(gidPayOperationRequest, gpbGidProperties)
            .let { gpbGidClient.pay(getTraceId(), it) }
            .run(gpbGidMapper::toBankPaymentPageData)
}
