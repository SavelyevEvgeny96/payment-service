package ru.sogaz.site.paymentService.model.v2.enums

enum class OperationState {
    NEW,
    REG,
    WAIT,
    SUCCESS,
    FAIL,
    ACCEPTED,
    REFUND,
    REVERSAL,
    DECLINED,
    CALLBACK,
    REJECTED,
    PERFORMED,
    FULLYPAID,
    NOT_STARTED
    ;

    fun isFinaleState() = this in arrayOf(SUCCESS, FAIL, REFUND, DECLINED)
}
