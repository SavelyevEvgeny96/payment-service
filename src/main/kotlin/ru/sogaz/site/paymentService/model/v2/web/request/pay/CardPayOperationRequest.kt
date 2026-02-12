package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.math.BigDecimal
import java.util.UUID

data class CardPayOperationRequest(
    override val orderId: UUID,
    override val description: String,
    override val amount: BigDecimal,
    override val payItems: Map<String, String> = emptyMap(),
    override val params: PayParams = PayParams(),
    val saveCard: Boolean,
) : PayOperationRequest() {
    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "bankCard",
    )
    override val paymentType: PaymentType = PaymentType.CARD
}
