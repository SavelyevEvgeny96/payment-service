package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class StatusEnum(
    @JsonValue val value: String,
) {
    NEW("NEW"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS"),
    ERROR("error"),
    OVERDUE("OVERDUE"),
    MARKEDDEL("MARKEDDEL"),
    FAIL("FAIL"),
    DECLINED("DECLINED"),
    UNKNOWN("UNKNOWN"),
    INTERIM_SUCCESS("INTERIM_SUCCESS"),
    REFUND("REFUND"),
    REG("REG"),
    WAIT("WAIT"),
    CALLBACK("CALLBACK"),
    ACCEPTED("ACCEPTED"),
    NOTSTARTED("NOT_STARTED"),
    RECEIVED("RECEIVED"),
    BLOCKED("BLOCKED"),
    REJECTED("REJECTED"),
    ;

    companion object {
        fun fromValue(value: String?): StatusEnum? = entries.find { it.value == value }
    }

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this == OVERDUE || this == MARKEDDEL
}
