package ru.sogaz.site.paymentService.model.v2.bank.response

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbExtResultCode
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import java.time.Instant

data class BankOperationDetails(
    @field:JsonProperty("id")
    val bankId: String?,
    @field:JsonProperty("status")
    val state: OperationState,
    val operationFinished: Instant? = null,
    val extendedCode: GpbExtResultCode? = null,
    val cardDetails: ClientCardDetails? = null,
    val errorText: String? = null,
)

data class ClientCardDetails(
    val maskedPan: String?, // Маскированный номер карты
    val paymentSystem: String?, // Наименование платёжной системы
    val issuerName: String?, // Кем выдана карта (банк-эмитент)
    val paymentType: String?, // Источник совершения операции (из portalType)
    val cardId: String?,
    val title: String?,
)

fun emptyCardDetails(keyCard: String) = ClientCardDetails(null, null, null, null, keyCard, null)
