package ru.sogaz.site.paymentService.enums

enum class OrderStatus(
    val value: String,
) {
    NEW("new"),
    UPDATE("update"),
    OVERDUE("overdue"),
    MARKEDDEL("markeddel"),
    SUCCESS("success"),
    CANCELED("canceled"),
    ;

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this in listOf(OVERDUE, MARKEDDEL)
}
