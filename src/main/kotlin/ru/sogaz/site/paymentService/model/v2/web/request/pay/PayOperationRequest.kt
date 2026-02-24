package ru.sogaz.site.paymentService.model.v2.web.request.pay

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.validation.constraint.SogazDomain
import java.math.BigDecimal

sealed class PayOperationRequest : OperationRequest() {
    abstract val description: String
    abstract val amount: BigDecimal
    abstract val payItems: LinkedHashMap<String, String>
    abstract val params: PayParams
}

data class PayParams(
    @field:Schema(description = "Ссылка для редиректа после оплаты", example = "https://www.sogaz.ru/")
    @field:SogazDomain
    val urlToReturn: String? = null,
    @field:Schema(description = "Ссылка для редиректа после успешной оплаты", example = "https://www.sogaz.ru/")
    @field:SogazDomain
    val urlToReturnS: String? = null,
    @field:Schema(description = "Ссылка для редиректа после неуспешной оплаты", example = "https://www.sogaz.ru/")
    @field:SogazDomain
    val urlToReturnF: String? = null,
    @field:Schema(description = "Флаг необходимости анонимизированной оплаты", defaultValue = "false")
    val depersonalization: Boolean = false,
)
