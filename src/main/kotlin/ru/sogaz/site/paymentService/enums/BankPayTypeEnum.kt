package ru.sogaz.site.paymentService.enums

enum class BankPayTypeEnum(
    val value: String,
) {
    PAYMENT_TYPE_PAY("bankCard"),
    PAYMENT_BANK_TYPE_SBP("sbp"),
}
