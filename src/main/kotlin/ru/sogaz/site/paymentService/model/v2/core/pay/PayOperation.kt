package ru.sogaz.site.paymentService.model.v2.core.pay

import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.core.Operation
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType

sealed class PayOperation : Operation() {
    override val operationType: OperationType
        get() = OperationType.REGISTRATION
    abstract val paymentType: PaymentType
    abstract val depersonalization: Boolean
    abstract val bank: BankEnum
    abstract val paymentBankId: String
}
