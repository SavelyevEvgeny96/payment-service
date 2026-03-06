package ru.sogaz.site.paymentService.model.v2.bank.exception

import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbExtResultCode

data class GpbCardPayErrorMessage(
    val error: GpbExtResultCode?,
    val side: String?,
)
