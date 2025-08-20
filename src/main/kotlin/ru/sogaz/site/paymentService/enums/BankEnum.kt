package ru.sogaz.site.paymentService.enums

enum class BankEnum(
    val value: String,
) {
    GPB("gpb"),
    AKB_RUS("akb_rus"),
    ;

    companion object {
        fun exists(value: String?): Boolean = entries.any { it.value == value }
    }
}
