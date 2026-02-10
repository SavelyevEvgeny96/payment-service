package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
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
    val urlToReturn: String? = null,
    val urlToReturnS: String? = null,
    val urlToReturnF: String? = null,
    val depersonalization: Boolean = false,
)
