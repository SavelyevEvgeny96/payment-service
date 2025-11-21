package ru.sogaz.site.paymentService.dto.response.bank

data class RegisterCardResponseDto(
    val token: String?,
    val state: String?,
    val result: ResultDto?,
) {
    data class ResultDto(
        val status: String?,
        val extendedCode: String?,
        val trxId: String?,
        val responseCode: String?,
        val rrn: String?,
        val approvalCode: String?,
        val orderStatus: String?,
        val orderStatusChangedAt: Long?,
    )
}
