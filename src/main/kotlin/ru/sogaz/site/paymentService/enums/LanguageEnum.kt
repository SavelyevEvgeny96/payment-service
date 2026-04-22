package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class LanguageEnum(
    @JsonValue val desc: String,
) {
    RU("ru"),
}
