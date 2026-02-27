package ru.sogaz.site.paymentService.model.v2.bank.properties.gpb

data class GpbSbpAutoPayHeaders(
    val paymentDelay: String?,
    val processPayments: String?,
    val paymentStatus: String?,
)
