package ru.sogaz.site.paymentService.enums

enum class StatusEnum(
    val value: String,
) {
    NEW("NEW"),
    SUCCESS("SUCCESS"),
    OVERDUE("OVERDUE"),
    MARKEDDEL("MARKEDDEL"),
    ;

    companion object {
        fun fromValue(value: String?): StatusEnum? = entries.find { it.value == value }
    }

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this == OVERDUE || this == MARKEDDEL
}
