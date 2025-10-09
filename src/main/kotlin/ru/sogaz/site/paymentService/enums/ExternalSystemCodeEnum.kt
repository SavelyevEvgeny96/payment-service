package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class ExternalSystemCodeEnum(
    val systemCode: String,
) {
    LK("LK"),
    ADI("ADI"),
    PAY("PAY"),
    FOP("FOP"),
    ONE_C("1C"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String?): ExternalSystemCodeEnum =
            ExternalSystemCodeEnum.entries
                .find { it.systemCode == value }
                ?: throw Exception("Invalid system code: $value")
    }
}
