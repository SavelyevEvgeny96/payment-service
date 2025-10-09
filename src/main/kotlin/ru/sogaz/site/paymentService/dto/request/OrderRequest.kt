package ru.sogaz.site.paymentService.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import ru.sogaz.site.paymentService.enums.TypeInsuranceEnum
import java.math.BigDecimal

/**
 * DTO для запроса на создание заказа.
 * Содержит все необходимые параметры для создания записи о платеже и отправки на оплату.
 *
 * @property operationId Идентификатор операции (обязательное поле)
 * @property premiumAmount Сумма премии (обязательное поле)
 * @property policyId Идентификатор полиса (обязательное поле)
 * @property policyNumber Номер полиса (обязательное поле)
 * @property externalSystemCode Код внешней системы, например, ADI, FOP, LK, 1C (обязательное поле)
 * @property typeInsurance Вид страхования (обязательное поле)
 * @property insuranceProgram Программа страхования
 * @property contractNumber Идентификатор договора
 * @property contractId Номер договора
 * @property docType Тип документа основания

 */
data class OrderRequest(
    @field:NotBlank(message = "{validation.orderRequest.operationId.notBlank}")
    val operationId: String = "",
    @field:Positive(message = "{validation.orderRequest.premiumAmount.positive}")
    val premiumAmount: BigDecimal = BigDecimal.ZERO,
    @field:NotBlank(message = "{validation.orderRequest.policyId.notBlank}")
    val policyId: String = "",
    @field:NotBlank(message = "{validation.orderRequest.policyNumber.notBlank}")
    val policyNumber: String = "",
    @field:NotNull(message = "{validation.orderRequest.externalSystemCode.notNull}")
    val externalSystemCode: ExternalSystemCodeEnum,
    @field:NotNull(message = "{validation.orderRequest.typeInsurance.notNull}")
    val typeInsurance: TypeInsuranceEnum,
    val insuranceProgram: String? = null,
    val mainContractCheck: Boolean = false,
    val contractNumber: String? = null,
    val contractId: String? = null,
    val docType: String? = null,
)
