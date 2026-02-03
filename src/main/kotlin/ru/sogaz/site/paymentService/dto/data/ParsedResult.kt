package ru.sogaz.site.paymentService.dto.data

sealed class ParsedResult<T> {
    data class Success<T>(
        val tag: Long,
        val dto: T,
        val messageId: String?,
    ) : ParsedResult<T>()

    data class Error<T>(
        val tag: Long,
        val rawBody: String,
        val payloadInfo: PayloadInfo,
        val messageId: String?,
        val cause: Throwable?,
    ) : ParsedResult<T>()
}
