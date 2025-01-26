package ru.sogaz.site.paymentService.dto

import ru.sogaz.site.paymentService.validation.anatationsConstraints.*
import java.math.BigDecimal

/**
 * DTO для запроса на создание платежа.
 * Содержит все необходимые параметры для создания записи о платеже и отправки на оплату.
 *
 * @property operationId Идентификатор операции (обязательное поле)
 * @property paymentEndDate Дата и время окончания действия ссылки на оплату (обязательное поле)
 * @property externalSystemCode Код внешней системы, например, ADI, FOP, LK, 1C (обязательное поле)
 * @property docType Тип документа основания (обязательное поле)
 * @property policyId Идентификатор полиса (обязательное поле)
 * @property policyNumber Номер полиса (обязательное поле)
 * @property contractNumber Идентификатор договора (необязательное поле)
 * @property contractId Номер договора (необязательное поле)
 * @property premiumAmount Сумма премии (необязательное поле)
 * @property recipientEmail Электронная почта получателя (обязательное поле)
 * @property needReceipt Признак необходимости отправки чека (необязательное поле)
 * @property recipientPhone Телефон получателя (обязательное поле)
 * @property policyholder ФИО страхователя (обязательное поле)
 * @property policyholderDoc Серия и номер паспорта страхователя (обязательное поле)
 * @property managerEmail Электронная почта менеджера (обязательное поле)
 * @property urlToReturn URL для перехода после успешной оплаты (обязательное поле)
 * @property urlToDecline URL для перехода после неуспешной оплаты (обязательное поле)
 * @property customURL Кастомный URL для страницы оплаты (необязательное поле)
 * @property hash Подпись целостности запроса (обязательное поле)
 * @property bank Банк для совершения операции (необязательное поле, если не указан — используется дефолтный банк из конфигурации)
 */
data class PaymentRequest(
    val operationId: String,
    val docType: String,
    val policyId: String,
    val policyNumber: String,
    val contractNumber: String?,
    val contractId: String?,
    val premiumAmount:  String?,
    val needReceipt: Boolean? = null,
    val urlToReturn: String,
    val urlToDecline: String,
    val customURL: String,
    val hash: String,
    @field:ExternalSystemCodeConstraint
    val externalSystemCode: String,
    @field:EmailConstraint
    val recipientEmail: String,
    @field:PhoneConstraint
    val recipientPhone: String,
    @field:PolicyholderConstraint
    val policyholder: String,
    @field:PolicyholderDocConstraint
    val policyholderDoc: String,
    @field:EmailConstraint
    val managerEmail: String,
    @field:PaymentEndDateConstraint
    val paymentEndDate: String,
    @field:BankConstraint
    val bank: String
)
