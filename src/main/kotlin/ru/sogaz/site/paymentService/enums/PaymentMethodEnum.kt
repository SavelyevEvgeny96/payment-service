package ru.sogaz.site.paymentService.enums

enum class PaymentMethodEnum(
    val value: String,
) {
    FULL_PAYMENT("full_payment"),
    ;

    companion object {
        fun fromValue(value: String?): PaymentTypeEnum? = PaymentTypeEnum.entries.find { it.value == value }
    }
}
