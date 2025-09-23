package ru.sogaz.site.paymentService.enums

enum class BankEnum(
    val value: String,
) {
    GPB("gpb"),
    AKB_RUS("akb_rus"),
    ;

    companion object {
        fun exists(value: String?): Boolean = entries.any { it.value == value }

        fun fromValue(value: String?): BankEnum? = BankEnum.entries.find { it.value == value }
    }
}
