package ru.sogaz.site.paymentService.dto.request

import jakarta.validation.constraints.*
import ru.sogaz.site.paymentService.enums.TypeInsuranceEnum
import ru.sogaz.site.paymentService.validation.constraint.Email
import java.math.BigDecimal
import java.time.Instant

/**
 * DTO для запроса на создание заказа.
 */
data class SubOrdersRequest(
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val operationId: String = "",
    @field:NotNull(message = "{validation.orderRequest.premiumAmount.notNull}")
    @field:Positive(message = "{validation.orderRequest.premiumAmount.positive}")
    val premiumAmount: BigDecimal = BigDecimal.ZERO,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val policyId: String = "",
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val policyNumber: String = "",
    @field:NotNull(message = "{validation.orderRequest.notBlank}")
    val typeInsurance: TypeInsuranceEnum,
    val insuranceProgram: String? = null,
    val mainContractCheck: Boolean = false,
    val contractNumber: String? = null,
    val contractId: String? = null,
    val docType: String? = null,
    @field:NotNull(message = "{validation.orderPaymentRequest.date.notNull}")
    @field:FutureOrPresent(message = "{validation.orderPaymentRequest.date.future}")
    val policyDate: Instant? = null,
    @field:NotNull(message = "{validation.orderPaymentRequest.date.notNull}")
    @field:FutureOrPresent(message = "{validation.orderPaymentRequest.date.future}")
    val contractDate: Instant? = null,
    @field:Email(message = "{validation.orderPaymentRequest.recipientEmail.email}")
    val managerEmail: String = "",
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val channel: String = "",
    val recurrent: Boolean = false,
)
