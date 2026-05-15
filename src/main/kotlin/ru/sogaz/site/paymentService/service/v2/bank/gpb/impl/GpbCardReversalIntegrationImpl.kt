package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbCardRefundClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardAccountManager
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardReversalIntegration

@Component
class GpbCardReversalIntegrationImpl(
    private val cardRefundClient: GpbCardRefundClient,
    private val accountManager: GpbCardAccountManager,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbCardResponseMapper,
) : GpbCardReversalIntegration {
    /**
     * Регистрирует возврат средств по платежу через GpbCardRefundClient.
     *
     * Метод:
     * 1. Определяет account по данным деперсонализации платежа
     * 2. Открывает сессию в GPB
     * 3. Инициирует возврат средств
     * 4. Гарантированно закрывает сессию (даже при ошибке выполнения)
     *
     * @param request данные для возврата средств (сумма, описание и пр.)
     *
     * @return ответ банка с результатом операции возврата
     *
     * @throws RuntimeException может пробрасывать исключения клиента GPB
     */
    override fun reversalPayCard(request: ReversalOperationRequest): BankOperationDetails {
        val account = accountManager.getByDepersonalization(request.depersonalization)
        val refundParams = requestMapper.toRefundParams(request)

        return cardRefundClient.startRefundSession(account).use {
            val response =
                cardRefundClient.refund(
                    account.portalId,
                    request.paymentBankId,
                    it.sessionToken,
                    refundParams,
                )
            responseMapper.refundToOperationDetails(request, response)
        }
    }

    private fun GpbCardRefundClient.startRefundSession(account: GpbCardAccountData) = GpbRefundSession(this, account).initSession()
}
