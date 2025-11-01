package ru.sogaz.site.paymentService.dto.data

data class ClientCardDetails(
    val maskedPan: String?, // Маскированный номер карты
    val paymentSystem: String?, // Наименование платёжной системы
    val issuerName: String?, // Кем выдана карта (банк-эмитент)
    val paymentType: String?, // Источник совершения операции (из portalType)
    val cardId: String?,
)
