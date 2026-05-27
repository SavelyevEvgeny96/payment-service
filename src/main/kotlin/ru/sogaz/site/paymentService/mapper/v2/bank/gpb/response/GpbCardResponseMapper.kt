package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.common.GpbPayStatusMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbRefundCardPayResponse
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

@Mapper(
    uses = [GpbCardDetailMapper::class, GpbPayStatusMapper::class],
    imports = [OperationState::class],
)
interface GpbCardResponseMapper {
    @Mapping(target = "paymentBankId", source = "token")
    @Mapping(target = "paymentPageUrl", source = "options.paymentPageUrl")
    @Mapping(target = "bank", constant = "GPB")
    fun toBankPaymentPageData(response: GpbPayCardResponse): BankPaymentPageData

    @Mapping(target = "state", source = "result.status", defaultValue = "WAIT")
    @Mapping(target = "extendedCode", source = "result.extendedCode")
    @Mapping(target = "errorText", source = "result.extendedCode.message")
    @Mapping(target = "cardDetails", source = "gpbCardDetails")
    @Mapping(target = "bankId", source = "id")
    @Mapping(target = "operationFinished", expression = "java( Instant.now() )")
    fun toBankPaymentDetails(response: GpbCardPayDetailsResponse): BankOperationDetails

    @Mapping(target = "bankId", source = "request.paymentBankId")
    @Mapping(target = "state", source = "response.status")
    @Mapping(target = "operationFinished", expression = "java( Instant.now() )")
    @Mapping(target = "errorText", source = "response.status")
    fun refundToOperationDetails(
        request: ReversalOperationRequest,
        response: GpbRefundCardPayResponse,
    ): BankOperationDetails
}
