package ru.sogaz.site.paymentService.mapper.v2.event

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.event.CompletedOperationEvent
import java.time.Instant

@Mapper(imports = [OperationState::class, Instant::class])
interface CompletedOperationMapper {
    @Mapping(target = "paymentId", source = "operation.id")
    @Mapping(target = "orderId", source = "operation.idempotentOrder.id")
    @Mapping(target = "payDate", expression = "java(Instant.now())")
    @Mapping(target = "totalAmount", source = "operation.premiumAmount")
    @Mapping(
        target = "status",
        source = "operationDetails.state",
        qualifiedByName = ["mapCompletedOperationStatus"],
    )
    @Mapping(target = "card", source = "operationDetails.cardDetails")
    @Mapping(target = "errorText", source = "operationDetails.errorText")
    @Mapping(
        target = "externalErrorCode",
        source = "operationDetails.extendedCode",
        conditionExpression = "java( operationDetails.getState() == OperationState.FAIL )",
    )
    fun completedOperationEvent(
        operation: IdempotentOrderOperation,
        operationDetails: BankOperationDetails,
    ): CompletedOperationEvent

    @Mapping(target = "paymentId", source = "operation.id")
    @Mapping(target = "orderId", source = "operation.idempotentOrder.id")
    @Mapping(target = "payDate", expression = "java(Instant.now())")
    @Mapping(target = "totalAmount", source = "operation.premiumAmount")
    @Mapping(
        target = "status",
        source = "operation.state",
        qualifiedByName = ["mapCompletedOperationStatus"],
    )
    fun completedOperationEvent(operation: IdempotentOrderOperation): CompletedOperationEvent

    @Named("mapCompletedOperationStatus")
    fun mapCompletedOperationStatus(state: OperationState): OperationState =
        when (state) {
            OperationState.SUCCESS,
            OperationState.REFUND,
            OperationState.REVERSAL,
            OperationState.ACCEPTED,
            OperationState.REJECTED,
            OperationState.PERFORMED,
            -> OperationState.SUCCESS

            OperationState.FAIL,
            OperationState.DECLINED,
            -> OperationState.FAIL

            else -> {
                state
            }
        }
}
