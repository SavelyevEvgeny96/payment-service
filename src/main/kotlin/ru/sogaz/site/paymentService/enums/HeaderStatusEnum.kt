package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class HeaderStatusEnum(
    @JsonValue val value: String,
) {
    PAYMENT_DELAY("paymentDelay"),
    PROCESS_PAYMENTS("processPayments"),
    PAYMENT_STATUS("paymentStatus"),
}
