package ru.sogaz.site.paymentService.model.v2.bank.response

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.model.v2.enums.OperationState

data class BankOperationDetails(
    @field:JsonProperty("id")
    val bankId: String,
    @field:JsonProperty("status")
    val state: OperationState,
    val extendedCode: String?,
    val cardDetails: ClientCardDetails? = null,
)

data class ClientCardDetails(
    val maskedPan: String?, // Маскированный номер карты
    val paymentSystem: String?, // Наименование платёжной системы
    val issuerName: String?, // Кем выдана карта (банк-эмитент)
    val paymentType: String?, // Источник совершения операции (из portalType)
    val cardId: String?,
    val title: String?,
)
