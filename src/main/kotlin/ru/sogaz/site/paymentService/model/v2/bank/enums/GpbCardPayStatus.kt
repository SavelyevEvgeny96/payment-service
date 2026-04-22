package ru.sogaz.site.paymentService.model.v2.bank.enums

enum class GpbCardPayStatus {
    NEW,
    FAILED,
    SUCCESS,
    OVERDUE,
    MARKEDDEL,
    FAIL,
    DECLINED,
    UNKNOWN,
    INTERIM_SUCCESS,
    REFUND,
    REG,
    WAIT,
    CALLBACK,
    ACCEPTED,
    NOTSTARTED,
    RECEIVED,
    BLOCKED,
    REJECTED,
}
