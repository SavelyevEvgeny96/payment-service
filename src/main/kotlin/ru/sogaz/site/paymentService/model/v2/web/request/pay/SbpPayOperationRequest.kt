package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.api.doc.v2.requestSchema.StraightRedirectSchema
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import java.math.BigDecimal
import java.util.UUID

data class SbpPayOperationRequest(
    @field:Schema(description = "Id заказа на оплату")
    override val orderId: UUID,
    @field:Schema(description = "Описание операции для банка", example = "Описание операции для банка")
    override val description: String,
    @field:Schema(description = "Сумма операции", example = "10.00")
    override val amount: BigDecimal,
    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "false")
    override val depersonalization: Boolean = false,
    @field:Schema(description = "Платежные позиции", accessMode = Schema.AccessMode.READ_ONLY)
    override val payItems: LinkedHashMap<String, String> = LinkedHashMap(),
    @field:Schema(description = "IP пользователя, который совершает оплату")
    val payerIp: String?,
    @field:Schema(description = "Дополнительные параметры банковской операции", implementation = StraightRedirectSchema::class)
    val params: RedirectParams = RedirectParams(),
) : PayOperationRequest() {
    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "PAY")
    override val operationType: OperationType = OperationType.PAY

    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "SBP")
    override val paymentType: PaymentType = PaymentType.SBP
}
