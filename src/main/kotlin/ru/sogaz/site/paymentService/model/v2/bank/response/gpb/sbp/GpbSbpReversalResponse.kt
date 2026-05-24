package ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp

data class GpbSbpReversalResponse(
    val code: String,
    val message: String,
    val transactionId: String,
) {
    fun isSuccess() = code == SUCCESS_CODE

    companion object {
        private const val SUCCESS_CODE = "000"
    }
}
