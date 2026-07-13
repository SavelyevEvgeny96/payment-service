package ru.sogaz.site.paymentService.model.v2.web.request.pay

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.math.BigDecimal
import java.util.UUID

/**
 * Запрос на инициирование оплаты банковской картой в ГИД через Газпромбанк.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GidPayOperationRequest(
    @field:Schema(description = "Идентификатор заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotNull(message = "{validation.payGid.orderId.notNull}")
    override val orderId: UUID,
    @field:Schema(description = "Назначение платежа", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "{validation.payGid.description.notBlank}")
    override val description: String,
    @field:Schema(description = "Сумма платежа в рублях", example = "2.68", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotNull(message = "{validation.payGid.amount.notNull}")
    override val amount: BigDecimal,
    @field:Schema(description = "Деперсонализация страницы оплаты банка")
    val depersonalization: Boolean = false,
    @field:Schema(description = "Признак необходимости сохранения карты")
    val saveCard: Boolean? = null,
    @field:Schema(description = "Список подсказок", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotNull(message = "{validation.payGid.payItems.notNull}")
    override val payItems: LinkedHashMap<String, String> = LinkedHashMap(),
    @field:Valid
    @field:NotNull(message = "{validation.payGid.params.notNull}")
    val params: GidPayParams,
) : PayOperationRequest() {
    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "PAY")
    override val operationType: OperationType = OperationType.PAY

    @field:Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "CARD_GID")
    override val paymentType: PaymentType = PaymentType.CARD_GID
}

/**
 * Дополнительные параметры оплаты банковской картой в ГИД.
 */
data class GidPayParams(
    @field:Schema(description = "Адрес возврата после успешной оплаты")
    val urlToReturnS: String? = null,
    @field:Schema(description = "Адрес возврата после неуспешной оплаты")
    val urlToReturnF: String? = null,
    @field:Schema(description = "Идентификатор пользователя в ГИД", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "{validation.payGid.gid.notBlank}")
    val gid: String,
    @field:Schema(description = "Идентификатор карты для оплаты в ГИД", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "{validation.payGid.cardId.notBlank}")
    val cardId: String,
)
