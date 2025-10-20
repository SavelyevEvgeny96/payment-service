package ru.sogaz.site.paymentService.enums

enum class OrderStatus {
    NEW,
    UPDATE,
    OVERDUE,
    MARKEDDEL,
    SUCCESS,
    CANCELED,
    ;

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this in listOf(OVERDUE, MARKEDDEL)
}
