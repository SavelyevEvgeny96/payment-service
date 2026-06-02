package ru.sogaz.site.paymentService.model.v2.bank.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class GpbSbpAutoPayHeader(
    @JsonValue val value: String,
) {
    PAYMENT_DELAY("paymentDelay"),
    PROCESS_PAYMENTS("processPayments"),
    PAYMENT_STATUS("paymentStatus"),
}
