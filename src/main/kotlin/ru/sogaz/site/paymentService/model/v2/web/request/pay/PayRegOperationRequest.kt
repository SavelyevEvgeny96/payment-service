package ru.sogaz.site.paymentService.model.v2.web.request.pay

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import java.math.BigDecimal
import java.util.UUID

data class PayRegOperationRequest(
    @field:Schema(description = "Id заказа на оплату")
    override val orderId: UUID,
    @field:Schema(description = "Описание операции для банка", example = "Описание операции для банка")
    override val description: String,
    @field:Schema(description = "Сумма операции", example = "10.00")
    override val amount: BigDecimal,
    @field:Schema(description = "Флаг необходимости анонимизированной оплаты", defaultValue = "false")
    override val depersonalization: Boolean,
    @field:Parameter(description = "IP пользователя, который совершает оплату")
    val payerIP: String? = null,
    @field:Schema(description = "Дополнительные параметры банковской операции")
    val params: RedirectParams = RedirectParams(),
    @field:Schema(description = "Флаг необходимости сохранения карты")
    val saveCard: Boolean,
) : PayOperationRequest() {
    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "REGISTRATION",
    )
    @JsonIgnore
    override val operationType: OperationType = OperationType.REGISTRATION

    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "CARD",
    )
    @JsonIgnore
    override val paymentType: PaymentType = PaymentType.CARD
}
