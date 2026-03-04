package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.math.BigDecimal
import java.util.UUID

data class CardRecurrentOperationRequest(
    @field:Schema(description = "Id заказа на оплату")
    override val orderId: UUID,
    @field:Schema(description = "Описание операции для банка", example = "Описание операции для банка")
    override val description: String,
    @field:Schema(description = "Сумма операции", example = "10.00")
    override val amount: BigDecimal,
    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "false")
    override val depersonalization: Boolean = false,
    @field:Schema(description = "Платежные позиции")
    override val payItems: LinkedHashMap<String, String> = LinkedHashMap(),
    @field:Schema(description = "Id карты сохраненной в банке")
    val keyCard: String,
) : PayOperationRequest() {
    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "RECURRENT",
    )
    override val operationType: OperationType = OperationType.RECURRENT

    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "CARD",
    )
    override val paymentType: PaymentType = PaymentType.CARD
}
