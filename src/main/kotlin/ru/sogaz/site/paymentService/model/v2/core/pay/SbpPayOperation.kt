package ru.sogaz.site.paymentService.model.v2.core.pay

import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.time.Instant
import java.util.UUID

data class SbpPayOperation(
    override val id: UUID,
    override val depersonalization: Boolean,
    override val bank: OperationBank,
    override val qrId: String,
    val operationStarted: Instant,
) : PayOperation() {
    override val paymentType: PaymentType = PaymentType.SBP
}
