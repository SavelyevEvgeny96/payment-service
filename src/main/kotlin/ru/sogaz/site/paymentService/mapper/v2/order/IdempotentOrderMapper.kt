package ru.sogaz.site.paymentService.mapper.v2.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest

@Mapper
interface IdempotentOrderMapper {
    fun toIdempotentOrder(operationRequest: OperationRequest): IdempotentOrder

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "depersonalization", source = "payOperationRequest.params.depersonalization")
    fun toIdempotentOrderOperation(
        idempotentOrder: IdempotentOrder,
        payOperationRequest: PayOperationRequest,
    ): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentBankId", source = "id")
    @Mapping(target = "paymentBankUrl", source = "paymentPageUrl")
    fun updateIdempotentOrderOperation(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        bankPaymentPageData: BankPaymentPageData,
    ): IdempotentOrderOperation
}
