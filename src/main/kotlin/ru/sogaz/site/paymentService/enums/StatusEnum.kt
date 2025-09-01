package ru.sogaz.site.paymentService.enums

enum class StatusEnum(
    val value: String,
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
    ;

    companion object {
        fun fromValue(value: String?): StatusEnum? = entries.find { it.value == value }
    }

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this == OVERDUE || this == MARKEDDEL
}
