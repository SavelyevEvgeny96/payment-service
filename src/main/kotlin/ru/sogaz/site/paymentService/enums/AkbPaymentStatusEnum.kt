package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class AkbPaymentStatusEnum(
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
    CLOSED("Closed"),
}
