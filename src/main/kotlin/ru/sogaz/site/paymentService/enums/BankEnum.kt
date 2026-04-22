package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class BankEnum(
    @JsonValue val code: String,
    val description: String,
) {
    GPB("gpb", "ГПБ"),
    ABR("abr", "АБР"),
    ;

    companion object {
        fun from(value: String?): BankEnum? =
            when (value) {
                "akb_rus" -> ABR
                else -> BankEnum.entries.find { it.code == value }
            }
    }
}
