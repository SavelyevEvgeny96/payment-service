package ru.sogaz.site.paymentService.model.v2.web.request.refund

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import java.math.BigDecimal
import java.util.UUID

data class RefundOperationRequest(
    override val orderId: UUID,
    override val amount: BigDecimal,
    override val paymentType: PaymentType,
    val bank: OperationBank,
    val paymentBankId: String,
    val description: String,
    val depersonalization: Boolean,
) : OperationRequest() {
    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "REFUND",
    )
    override val operationType: OperationType = OperationType.REFUND
}
