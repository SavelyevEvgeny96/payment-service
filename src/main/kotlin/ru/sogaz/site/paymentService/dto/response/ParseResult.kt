package ru.sogaz.site.paymentService.dto.response

import org.springframework.amqp.core.Message
import ru.sogaz.site.paymentService.dto.data.TaggedPayload

sealed class ParseResult<out T> {
    data class Success<T>(
        val payload: TaggedPayload<T>,
    ) : ParseResult<T>()

    data class Failure(
        val message: Message,
        val exception: Throwable,
    ) : ParseResult<Nothing>()
}
