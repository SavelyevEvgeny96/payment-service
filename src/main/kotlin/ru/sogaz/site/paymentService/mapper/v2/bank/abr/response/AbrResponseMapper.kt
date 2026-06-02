package ru.sogaz.site.paymentService.mapper.v2.bank.abr.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.dto.response.AbrOrderResponse
import ru.sogaz.site.paymentService.dto.response.PaymentAbrStatusResponse
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.enums.AbrPaymentStatusEnum
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import java.time.Instant

@Mapper(
    imports = [Instant::class],
)
abstract class AbrResponseMapper {
    companion object {
        private const val IPS_RU_PARAM_NAME = "ipsRu"
        private const val EMPTY_REDIRECT_URL_RESPONSE = "Пустой REDIRECT URL в ответе АБР Россия"

        @JvmStatic
        @Named("mapQrcPayload")
        fun mapQrcPayload(preparePushTranResponse: PreparePushTranResponse): String =
            requireNotNull(preparePushTranResponse.getQrcPayload(IPS_RU_PARAM_NAME)) {
                EMPTY_REDIRECT_URL_RESPONSE
            }

        @JvmStatic
        @Named("mapCurrentStatus")
        fun mapCurrentStatus(response: PaymentAbrStatusResponse): OperationState = response.getCurrentStatus().toOperationState()

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

    @Mapping(target = "bank", constant = "ABR")
    @Mapping(target = "paymentBankId", source = "order.id")
    @Mapping(target = "paymentPageUrl", source = "order.hppUrl")
    @Mapping(target = "paymentPass", source = "order.password")
    abstract fun toCardPaymentPageData(response: AbrOrderResponse): BankPaymentPageData

    @Mapping(target = "bank", constant = "ABR")
    @Mapping(target = "paymentBankId", source = "orderResponse.order.id")
    @Mapping(target = "qrId", source = "orderResponse.order.id")
    @Mapping(target = "paymentPageUrl", source = "preparePushTranResponse", qualifiedByName = ["mapQrcPayload"])
    @Mapping(target = "paymentPass", source = "orderResponse.order.password")
    abstract fun toSbpPaymentPageData(
        orderResponse: AbrOrderResponse,
        preparePushTranResponse: PreparePushTranResponse,
    ): BankPaymentPageData

    @Mapping(target = "bankId", source = "order.id")
    @Mapping(target = "state", source = ".", qualifiedByName = ["mapCurrentStatus"])
    @Mapping(target = "operationFinished", expression = "java( Instant.now() )")
    abstract fun toBankOperationDetails(response: PaymentAbrStatusResponse): BankOperationDetails
}
