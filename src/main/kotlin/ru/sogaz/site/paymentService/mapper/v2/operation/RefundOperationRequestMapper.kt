package ru.sogaz.site.paymentService.mapper.v2.operation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.RefundEvent
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest

@Mapper
interface RefundOperationRequestMapper {
    @Mapping(target = "description", source = "refundEvent.description")
    @Mapping(target = "bank", source = "refundEvent.bank")
    @Mapping(target = "paymentBankId", source = "refundEvent.paymentBankId")
    @Mapping(target = "amount", source = "refundEvent.amount")
    @Mapping(target = "orderId", source = "idempotentOrderOperation.idempotentOrder.id")
    fun toRefundOperationRequest(
        idempotentOrderOperation: IdempotentOrderOperation,
        refundEvent: RefundEvent,
    ): RefundOperationRequest
}
