package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class TypeInsuranceEnum(
    val description: String,
) {
    OSAGO("ОСАГО"),
    AUTO_KASKO("Автокаско"),
    TRAVEL("Путешествия"),
    ACCIDENT("От несчастных случаев"),
    HOUSE("Дом"),
    APARTMENT("Квартира"),
    DMS("ДМС"),
    MORTGAGE("Ипотека"),
    OMS("ОМС"),
    LIFE("Страхование жизни"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String?): TypeInsuranceEnum =
            TypeInsuranceEnum.entries
                .find { it.description == value }
                ?: throw Exception("Invalid insurance type: $value")
    }
}
