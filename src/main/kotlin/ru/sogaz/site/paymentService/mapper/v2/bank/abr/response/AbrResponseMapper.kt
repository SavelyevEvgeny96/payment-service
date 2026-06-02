package ru.sogaz.site.paymentService.mapper.v2.bank.abr.response

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.response.AbrOrderResponse
import ru.sogaz.site.paymentService.dto.response.PaymentAbrStatusResponse
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.enums.AbrPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import java.time.Instant

@Component
class AbrResponseMapper {
    companion object {
        private const val IPS_RU_PARAM_NAME = "ipsRu"
        private const val EMPTY_REDIRECT_URL_RESPONSE = "Пустой REDIRECT URL в ответе АБР Россия"
    }

    fun toCardPaymentPageData(response: AbrOrderResponse): BankPaymentPageData =
        BankPaymentPageData(
            bank = BankEnum.ABR,
            paymentBankId = response.order.id.toString(),
            paymentPageUrl = response.order.hppUrl,
            paymentPass = response.order.password,
        )

    fun toSbpPaymentPageData(
        orderResponse: AbrOrderResponse,
        preparePushTranResponse: PreparePushTranResponse,
    ): BankPaymentPageData =
        BankPaymentPageData(
            bank = BankEnum.ABR,
            paymentBankId = orderResponse.order.id.toString(),
            qrId = orderResponse.order.id.toString(),
            paymentPageUrl = requireNotNull(preparePushTranResponse.getQrcPayload(IPS_RU_PARAM_NAME)) {
                EMPTY_REDIRECT_URL_RESPONSE
            },
            paymentPass = orderResponse.order.password,
        )

    fun toBankOperationDetails(response: PaymentAbrStatusResponse): BankOperationDetails =
        BankOperationDetails(
            bankId = response.order.id,
            state = response.getCurrentStatus().toOperationState(),
            operationFinished = Instant.now(),
        )

    private fun PaymentAbrStatusResponse.getCurrentStatus(): AbrPaymentStatusEnum {
        if (order.status != AbrPaymentStatusEnum.CLOSED) {
            return order.status
        }
        return when (order.prevStatus) {
            AbrPaymentStatusEnum.PREPARING,
            AbrPaymentStatusEnum.WAITPUSHTRAN,
            AbrPaymentStatusEnum.AUTHORIZED,
            -> AbrPaymentStatusEnum.PREPARING

            else -> AbrPaymentStatusEnum.CLOSED
        }
    }

    private fun AbrPaymentStatusEnum.toOperationState(): OperationState =
        when (this) {
            AbrPaymentStatusEnum.PARTPAID,
            AbrPaymentStatusEnum.REFUNDED,
            AbrPaymentStatusEnum.VOIDED,
            -> OperationState.REFUND

            AbrPaymentStatusEnum.DECLINED,
            AbrPaymentStatusEnum.EXPIRED,
            -> OperationState.FAIL

            AbrPaymentStatusEnum.REFUSED -> OperationState.DECLINED
            AbrPaymentStatusEnum.FULLYPAID -> OperationState.SUCCESS
            else -> OperationState.WAIT
        }
}
