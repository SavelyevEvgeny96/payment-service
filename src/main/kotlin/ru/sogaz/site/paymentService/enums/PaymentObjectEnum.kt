package ru.sogaz.site.paymentService.enums

enum class PaymentObjectEnum(
    val value: String,
) {
    PAYMENT_OBJECT_SERVICE("service"),
    ;

    companion object {
        fun fromValue(value: String?): PaymentTypeEnum? = PaymentTypeEnum.entries.find { it.value == value }
    }
}
