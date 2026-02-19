package ru.sogaz.site.paymentService.enums

enum class PaymentStatusEnum {
    NEW,
    REG,
    WAIT,
    SUCCESS,
    FAIL,
    REFUND,
    DECLINED,
    CALLBACK;

    fun isInProcess(): Boolean = this in listOf(REG, WAIT)

    fun isClosed(): Boolean = isInProcess().not()

    fun isSuccess(): Boolean = this in listOf(SUCCESS, REFUND)

    fun isFail(): Boolean = this in listOf(FAIL, DECLINED)
}