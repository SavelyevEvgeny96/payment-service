package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class BankEnum(
    @JsonValue val code: String,
) {
    GPB("gpb"),
    AKB_RUS("akb_rus"),
    ;

    companion object {
        fun from(value: String?): BankEnum? = BankEnum.entries.find { it.code == value }
    }
}
