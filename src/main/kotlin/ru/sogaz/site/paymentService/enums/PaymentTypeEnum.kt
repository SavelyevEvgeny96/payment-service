package ru.sogaz.site.paymentService.enums

enum class PaymentTypeEnum(
    val value: String,
) {
    CARD("bankCard"),
    SBP("sbp"),
    ;

    companion object {
        fun fromValue(value: String?): PaymentTypeEnum? = PaymentTypeEnum.entries.find { it.value == value }
    }
}
