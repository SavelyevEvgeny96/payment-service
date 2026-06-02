package ru.sogaz.site.paymentService.mapper.v2.bank.abr.request

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.request.AbrCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.dto.request.OrderDto
import ru.sogaz.site.paymentService.dto.request.PreparePushTranRequest
import ru.sogaz.site.paymentService.dto.request.SetSrcTokenRequest
import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.enums.TypeRidEnum
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class AbrRequestMapper(
    private val apiConfigProperties: ApiConfigProperties,
) {
    companion object {
        private const val REDIRECT_URL_PARAM_NAME = "afterPayRedirectUrl"
        private const val IPS_RU_PARAM_NAME = "ipsRu"
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun toCardRequest(cardPayOperationRequest: CardPayOperationRequest): AbrCardAndSbpPaymentRequest =
        cardPayOperationRequest.toAbrRequest(TypeRidEnum.WITH_3DS)

    fun toSbpRequest(sbpPayOperationRequest: SbpPayOperationRequest): AbrCardAndSbpPaymentRequest =
        sbpPayOperationRequest.toAbrRequest(TypeRidEnum.QRC_PAY)

    fun toSetSrcTokenRequest(): SetSrcTokenRequest = SetSrcTokenRequest(token = mapOf(IPS_RU_PARAM_NAME to true))

    fun toPreparePushTranRequest(redirectParams: RedirectParams): PreparePushTranRequest =
        PreparePushTranRequest(
            specificByPm = mapOf(
                IPS_RU_PARAM_NAME to mapOf(REDIRECT_URL_PARAM_NAME to redirectParams.successUrl()),
            ),
        )

    private fun CardPayOperationRequest.toAbrRequest(typeRid: TypeRidEnum): AbrCardAndSbpPaymentRequest =
        buildAbrRequest(
            orderId = orderId,
            amountData = AmountData(amount, CurrencyEnum.RUB),
            description = description,
            redirectParams = params,
            typeRid = typeRid,
        )

    private fun SbpPayOperationRequest.toAbrRequest(typeRid: TypeRidEnum): AbrCardAndSbpPaymentRequest =
        buildAbrRequest(
            orderId = orderId,
            amountData = AmountData(amount, CurrencyEnum.RUB),
            description = description,
            redirectParams = params,
            typeRid = typeRid,
        )

    private fun buildAbrRequest(
        orderId: UUID,
        amountData: AmountData,
        description: String,
        redirectParams: RedirectParams,
        typeRid: TypeRidEnum,
    ) = OrderDto(
        typeRid = typeRid,
        ridByMerchant = orderId.toString(),
        amount = amountData.getAmount(),
        currency = amountData.currency,
        hppRedirectUrl = redirectParams.successUrl(),
        adviceIfaceAddress = redirectParams.successUrl(),
        description = description,
        descriptionHtml = description,
        expTime = plus15MinutesFromNow().takeIf { typeRid == TypeRidEnum.QRC_PAY },
    ).run(::AbrCardAndSbpPaymentRequest)

    private fun RedirectParams.successUrl(): String = urlToReturnS ?: urlToReturn ?: apiConfigProperties.backUrlS

    private fun plus15MinutesFromNow() = LocalDateTime.now().plusMinutes(15).format(formatter)
}
