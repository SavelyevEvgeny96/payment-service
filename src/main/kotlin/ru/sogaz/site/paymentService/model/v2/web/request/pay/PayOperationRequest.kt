package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.validation.constraint.SogazDomain
import java.math.BigDecimal

sealed class PayOperationRequest : OperationRequest() {
    @field:Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        defaultValue = "PAY",
    )
    override val operationType: OperationType = OperationType.PAY
    abstract val paymentType: PaymentType
    abstract val description: String
    abstract val amount: BigDecimal
    abstract val payItems: Map<String, String>
    abstract val params: PayParams
}

data class PayParams(
    @field:Schema(description = "Ссылка для редиректа после оплаты", example = "http://sogaz.ru")
    @field:SogazDomain
    val urlToReturn: String? = null,
    @field:Schema(description = "Ссылка для редиректа после успешной оплаты", example = "http://sogaz.ru")
    @field:SogazDomain
    val urlToReturnS: String? = null,
    @field:Schema(description = "Ссылка для редиректа после неуспешной оплаты", example = "http://sogaz.ru")
    @field:SogazDomain
    val urlToReturnF: String? = null,
    @field:Schema(description = "Флаг необходимости анонимизированной оплаты")
    val depersonalization: Boolean = false,
)
