package ru.sogaz.site.paymentService.model.v2.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.enums.payment.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.payment.PaymentType
import java.math.BigDecimal
import java.util.UUID

data class PayRequest(
    val orderId: UUID,
    @field:NotBlank
    val description: String,
    val payItems: Map<String, String> = emptyMap(),
    @field:Positive
    val amount: BigDecimal,
    val params: PayParams = PayParams(),
) {
    lateinit var paymentType: PaymentType
    lateinit var operationType: OperationType
    lateinit var bank: BankEnum
}
