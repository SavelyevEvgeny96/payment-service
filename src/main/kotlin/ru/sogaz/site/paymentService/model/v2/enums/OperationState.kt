package ru.sogaz.site.paymentService.model.v2.enums

enum class OperationState {
    NEW,
    REG,
    WAIT,
    SUCCESS,
    FAIL,
    REFUND,
    DECLINED,
    CALLBACK,
    ;

    fun isFinaleState() = this in arrayOf(SUCCESS, FAIL, REFUND, DECLINED)
}
