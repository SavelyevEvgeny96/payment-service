package ru.sogaz.site.paymentService.dto

import jakarta.validation.constraints.NotNull

/**
 * @property recipientEmail Электронная почта получателя (обязательное поле)
 * @property needReceipt Признак необходимости отправки чека (необязательное поле)
 * @property recipientPhone Телефон получателя (обязательное поле)
 * @property policyHolder ФИО страхователя (обязательное поле)
 * @property policyHolderDoc Серия и номер паспорта страхователя (обязательное поле)
 * @property bank Банк для совершения операции (необязательное поле, если не указан — используется дефолтный банк из конфигурации)
 * @property urlToReturn URL для перехода после успешной оплаты (обязательное поле)
 * @property urlToDecline URL для перехода после неуспешной оплаты (обязательное поле)
 * @property customURL Кастомный URL для страницы оплаты (необязательное поле)
 * @property paymentEndDate Дата и время окончания действия ссылки на оплату (обязательное поле)
 * @property payments Лист данных по отдельному payments(каждый payments имеет отдельную запись в таблице subOrder)

 */
data class PaymentRequestWrapper(
    @NotNull
    val payments: List<PaymentRequest>,
    val urlToReturn: String?,
    val urlToDecline: String?,
    val customURL: String?,
    val bank: String?,
    @NotNull
    val paymentEndDate: String,
    @NotNull
    val recipientEmail: String,
    @NotNull
    val recipientPhone: String?,
    val policyHolder: String?,
    val policyHolderDoc: String?,
    val recipientUserId: String?,
    val needReceipt: Boolean?,
)
