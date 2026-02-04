package ru.sogaz.site.paymentService.model.v2.enums.payment

enum class PaymentState {
    NEW,
    REG,
    WAIT,
    SUCCESS,
    FAIL,
    REFUND,
    DECLINED,
    CALLBACK,
}
