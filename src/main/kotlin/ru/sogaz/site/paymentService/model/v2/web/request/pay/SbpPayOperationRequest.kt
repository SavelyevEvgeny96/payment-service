package ru.sogaz.site.paymentService.model.v2.web.request.pay

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import ru.sogaz.site.paymentService.api.doc.v2.requestSchema.StraightRedirectSchema
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentRequestBank
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SbpPayOperationRequest(
    @field:Schema(description = "Id заказа на оплату", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank
    override val orderId: UUID,
    @field:Schema(
        description = "Описание операции для банка",
        example = "Оплата по договору SF-000011950 от 15.09.2026. Платежный сервис, дата операции 13.02.2026",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotBlank
    override val description: String,
    @field:Schema(description = "Сумма операции", example = "10.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank
    override val amount: BigDecimal,
    @field:Schema(
        description = "IP пользователя, который совершает оплату",
        example = "127.0.0.1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val payerIp: String? = null,
    @field:Schema(description = "Вид страхования", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    override val insuranceKind: String? = null,
    @field:Schema(
        description = "Приоритетный банк для оплаты",
        allowableValues = ["GPB", "ABR"],
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    override val bank: PaymentRequestBank? = null,
    @field:Schema(description = "Платежные позиции", accessMode = Schema.AccessMode.READ_ONLY)
    override val payItems: LinkedHashMap<String, String> = LinkedHashMap(),
    @field:Schema(
        description = "Дополнительные параметры банковской операции",
        implementation = StraightRedirectSchema::class,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotBlank
    val params: RedirectParams = RedirectParams(),
) : PayOperationRequest() {
    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "PAY")
    override val operationType: OperationType = OperationType.PAY

    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "SBP")
    override val paymentType: PaymentType = PaymentType.SBP
}
