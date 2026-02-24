package ru.sogaz.site.paymentService.mapper.v2.operation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

@Mapper
interface PayOperationRequestMapper {
    @Mapping(target = "depersonalization", source = "payOperationRequest.params.depersonalization")
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "REG")
    fun toIdempotentOrderOperation(payOperationRequest: PayOperationRequest): IdempotentOrderOperation

    @Mapping(target = "state", constant = "REG")
    @Mapping(target = "paymentBankUrl", source = "paymentPageUrl")
    fun updateByBankPaymentPage(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        bankPaymentPageData: BankPaymentPageData,
    ): IdempotentOrderOperation
}
