package ru.sogaz.site.paymentService.dto.data
sealed class ParsedResult<out T> {

    data class Success<T>(
        val tag: Long,
        val dto: T,
        val messageId: String?,
    ) : ParsedResult<T>()

    data class Error(
        val tag: Long,
        val rawBody: String,
        val payloadInfo: PayloadInfo,
        val messageId: String?,
        val cause: Throwable?,
    ) : ParsedResult<Nothing>()
}
