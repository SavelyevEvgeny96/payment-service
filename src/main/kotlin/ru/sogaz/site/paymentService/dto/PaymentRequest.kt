package ru.sogaz.site.paymentService.dto

import jakarta.validation.constraints.NotNull

/**
 * DTO для запроса на создание платежа.
 * Содержит все необходимые параметры для создания записи о платеже и отправки на оплату.
 *
 * @property externalSystemCode Код внешней системы, например, ADI, FOP, LK, 1C (обязательное поле)
 * @property docType Тип документа основания (обязательное поле)
 * @property policyId Идентификатор полиса (обязательное поле)
 * @property policyNumber Номер полиса (обязательное поле)
 * @property contractNumber Идентификатор договора (необязательное поле)
 * @property contractId Номер договора (необязательное поле)
 * @property premiumAmount Сумма премии (необязательное поле)
 * @property managerEmail Электронная почта менеджера (обязательное поле)
 * @property hash Подпись целостности запроса (обязательное поле)

 */
data class PaymentRequest(
    @NotNull
    val operationId: String,
    val docType: String,
    @NotNull
    val policyId: String,
    @NotNull
    val policyNumber: String,
    val contractNumber: String,
    val contractId: String,
    @NotNull
    val premiumAmount: String,
    val hash: String?,
    @NotNull
    val externalSystemCode: String,
    val managerEmail: String,
    val typeInsurance: String?,
    val insuranceProgram: String?,
    var traceId: String?,
)
