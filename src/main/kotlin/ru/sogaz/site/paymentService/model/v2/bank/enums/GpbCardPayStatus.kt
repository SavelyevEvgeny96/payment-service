package ru.sogaz.site.paymentService.model.v2.bank.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class GpbCardPayStatus(
    @JsonValue val value: String,
) {
    NEW("NEW"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS"),
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
}
