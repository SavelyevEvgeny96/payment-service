package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class BankEnum(
    val code: String,
) {
    GPB("gpb"),
    AKB_RUS("akb_rus"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String?): BankEnum =
            BankEnum.entries
                .find { it.code == value }
                ?: throw Exception("Invalid bank enum: $value")
    }
}
