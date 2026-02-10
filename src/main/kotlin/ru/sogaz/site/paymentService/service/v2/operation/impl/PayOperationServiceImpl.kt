package ru.sogaz.site.paymentService.service.v2.operation.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardPayIntegration
import ru.sogaz.site.paymentService.service.v2.operation.PayOperationService
import ru.sogaz.site.paymentService.service.v2.strategy.pay.PayStrategy
import ru.sogaz.site.paymentService.service.v2.strategy.pay.gpb.CardPayStrategy

@Service
class PayOperationServiceImpl(
    private val gpbCardPayIntegration: GpbCardPayIntegration,
) : PayOperationService {
    override fun payOperation(payOperationRequest: PayOperationRequest): PayStrategy =
        when (payOperationRequest) {
            is CardPayOperationRequest -> cardPayStrategy(payOperationRequest)
            is SbpPayOperationRequest -> sbpPayStrategy(payOperationRequest)
        }

    private fun cardPayStrategy(cardPayOperationRequest: CardPayOperationRequest): PayStrategy =
        CardPayStrategy(cardPayOperationRequest, gpbCardPayIntegration)

    private fun sbpPayStrategy(sbpPayOperationRequest: SbpPayOperationRequest): PayStrategy =
        TODO("Not yet implemented")
}
