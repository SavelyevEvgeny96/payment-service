package ru.sogaz.site.paymentService.mapper.v2.operation

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpReversalPayOperation
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType

@Mapper
abstract class OperationMapper {
    fun makePayOperation(idempotentOrderOperation: IdempotentOrderOperation): PayOperation =
        when (idempotentOrderOperation.paymentType) {
            PaymentType.CARD -> makeCardPayOperation(idempotentOrderOperation)

            PaymentType.SBP ->
                when (idempotentOrderOperation.operationType) {
                    OperationType.PAY -> makeSbpPayOperation(idempotentOrderOperation)

                    OperationType.REVERSAL -> makeSbpReversalPayOperation(idempotentOrderOperation)

                    OperationType.RECURRENT,
                    OperationType.REGISTRATION,
                    -> TODO("SBP ${idempotentOrderOperation.operationType} is not implemented")
                }
        }

    abstract fun makeCardPayOperation(idempotentOrderOperation: IdempotentOrderOperation): CardPayOperation

    abstract fun makeSbpPayOperation(idempotentOrderOperation: IdempotentOrderOperation): SbpPayOperation

    abstract fun makeSbpReversalPayOperation(idempotentOrderOperation: IdempotentOrderOperation): SbpReversalPayOperation
}
