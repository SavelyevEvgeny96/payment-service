package ru.sogaz.site.paymentService.mapper.v2.operation

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.model.v2.core.Operation
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType

@Mapper
abstract class OperationMapper {
    fun makeOperation(idempotentOrderOperation: IdempotentOrderOperation): Operation =
        when (idempotentOrderOperation.operationType) {
            OperationType.PAY -> makePayOperation(idempotentOrderOperation)
            OperationType.RECURRENT -> TODO()
            OperationType.CARD_REGISTRATION -> TODO()
        }

    fun makePayOperation(idempotentOrderOperation: IdempotentOrderOperation): PayOperation =
        when (idempotentOrderOperation.paymentType) {
            PaymentType.CARD -> makeCardPayOperation(idempotentOrderOperation)
            PaymentType.SBP -> TODO()
        }

    abstract fun makeCardPayOperation(idempotentOrderOperation: IdempotentOrderOperation): CardPayOperation
}
