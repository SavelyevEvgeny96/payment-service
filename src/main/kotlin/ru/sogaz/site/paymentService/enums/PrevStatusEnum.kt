package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class PrevStatusEnum(
    @JsonValue val value: String,
) {
    PREPARING("Preparing"),
    WAITPUSHTRAN("WaitPushTran"),
    AUTHORIZED("Authorized"),
    PARTPAID("PartPaid"),
    REFUNDED("Refunded"),
    VOIDED("Voided"),
    DECLINED("Declined"),
    EXPIRED("Expired"),
    REFUSED("Refused"),
    FULLYPAID("FullyPaid"),
    ;

    companion object {
        fun fromValue(value: String?): PrevStatusEnum? = PrevStatusEnum.entries.find { it.value == value }
    }
}
