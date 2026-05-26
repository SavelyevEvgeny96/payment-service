package ru.sogaz.site.paymentService.model.v2.core.pay

import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.util.UUID

data class CardPayOperation(
    override val id: UUID,
    override val depersonalization: Boolean,
    override val bank: OperationBank,
    override val paymentBankId: String,
) : PayOperation() {
    override val paymentType: PaymentType = PaymentType.CARD
}
