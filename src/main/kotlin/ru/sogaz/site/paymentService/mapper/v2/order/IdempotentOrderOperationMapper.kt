package ru.sogaz.site.paymentService.mapper.v2.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

@Mapper
interface IdempotentOrderOperationMapper {
    @Mapping(target = "id", source = "orderId")
    fun toIdempotentOrder(operationRequest: OperationRequest): IdempotentOrder

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "premiumAmount", constant = "0")
    @Mapping(target = "bank", constant = "GPB")
    fun toGpbIdempotentOrderOperation(operationRequest: OperationRequest): IdempotentOrderOperation

    @Mapping(target = "depersonalization", source = "payOperationRequest.params.depersonalization")
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "REG")
    @Mapping(target = "bank", constant = "GPB")
    fun toGpbIdempotentOrderOperation(payOperationRequest: PayOperationRequest): IdempotentOrderOperation

    @Mapping(target = "paymentBankId", source = "token")
    @Mapping(target = "state", constant = "REG")
    fun updateByAuthorizedTrx(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): IdempotentOrderOperation

    @Mapping(target = "state", constant = "REG")
    @Mapping(target = "paymentBankUrl", source = "paymentPageUrl")
    fun updateByBankPaymentPage(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        bankPaymentPageData: BankPaymentPageData,
    ): IdempotentOrderOperation
}
