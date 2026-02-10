package ru.sogaz.site.paymentService.model.v2.web.request.pay

import ru.sogaz.site.paymentService.model.v2.enums.payment.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.PayParams
import java.math.BigDecimal
import java.util.UUID

data class CardPayOperationRequest(
    override val orderId: UUID,
    override val description: String,
    override val amount: BigDecimal,
    override val payItems: Map<String, String> = emptyMap(),
    override val params: PayParams = PayParams(),
) : PayOperationRequest() {
    override val paymentType: PaymentType = PaymentType.CARD
}
