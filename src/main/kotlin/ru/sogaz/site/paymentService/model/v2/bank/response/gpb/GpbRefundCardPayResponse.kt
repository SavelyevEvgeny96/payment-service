package ru.sogaz.site.paymentService.model.v2.bank.response.gpb

import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbRefundStatus

data class GpbRefundCardPayResponse(
    val status: GpbRefundStatus,
    val action: String,
    val rrn: String,
    val approvalCode: String,
    val refundToken: String,
)
