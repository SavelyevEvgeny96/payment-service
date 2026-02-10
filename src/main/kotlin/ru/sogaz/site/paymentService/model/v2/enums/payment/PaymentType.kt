package ru.sogaz.site.paymentService.model.v2.enums.payment

import com.fasterxml.jackson.annotation.JsonValue

enum class PaymentType(
    @JsonValue val desc: String,
) {
    CARD("bankCard"),
    SBP("sbp"),
}
