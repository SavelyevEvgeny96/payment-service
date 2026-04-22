package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class TypeRidEnum(
    @JsonValue val desc: String,
) {
    QRC_PAY("QRC_PAY"),
    WITH_3DS("WITH_3DS"),
}
