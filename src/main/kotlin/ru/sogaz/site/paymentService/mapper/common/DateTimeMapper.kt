package ru.sogaz.site.paymentService.mapper.common

import org.mapstruct.Named
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class DateTimeMapper {
    @Named("instantToLocalDateTime")
    fun instantToLocalDateTime(paymentEndDate: Instant): LocalDateTime = LocalDateTime.ofInstant(paymentEndDate, ZoneId.systemDefault())

    @Named("localDateTimeToFormattedString")
    fun localDateTimeToFormattedString(dateTime: LocalDateTime): String =
        dateTime
            .atZone(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}
