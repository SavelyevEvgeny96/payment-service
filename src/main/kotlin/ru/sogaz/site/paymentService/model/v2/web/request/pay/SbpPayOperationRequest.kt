package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.math.BigDecimal
import java.util.UUID

data class SbpPayOperationRequest(
    @field:Schema(description = "Id заказа на оплату")
    override val orderId: UUID,
    @field:Schema(description = "Описание операции для банка", example = "Описание операции для банка")
    override val description: String,
    @field:Schema(description = "Сумма операции", example = "10", type = "integer")
    override val amount: BigDecimal,
    @field:Schema(description = "Платежные позиции")
    override val payItems: Map<String, String> = emptyMap(),
    @field:Schema(description = "Дополнительные параметры операции")
    override val params: PayParams = PayParams(),
) : PayOperationRequest() {
    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "sbp",
    )
    override val paymentType: PaymentType = PaymentType.SBP
}
