package ru.sogaz.site.paymentService.enums

enum class OrderStatus {
    NEW,
    UPDATE,
    OVERDUE,
    MARKEDDEL,
    SUCCESS,
    ;

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this in listOf(OVERDUE, MARKEDDEL)
}
