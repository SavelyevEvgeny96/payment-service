package ru.sogaz.site.paymentService.mapper.v2.event

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CompletedOperationEvent

@Mapper
interface CompletedOperationMapper {
    @Mapping(target = "paymentId", source = "operation.id")
    @Mapping(target = "orderId", source = "operation.idempotentOrder.orderId")
    @Mapping(target = "payDate", expression = "java(Instant.now())")
    @Mapping(target = "totalAmount", constant = "0")
    @Mapping(target = "status", source = "operationDetails.state")
    @Mapping(target = "keyCard", source = "operationDetails.cardDetails.cardId")
    fun completedOperationEvent(
        operation: IdempotentOrderOperation,
        operationDetails: BankOperationDetails,
    ): CompletedOperationEvent
}
