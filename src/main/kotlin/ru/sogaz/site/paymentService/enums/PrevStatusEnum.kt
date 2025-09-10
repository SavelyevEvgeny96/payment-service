package ru.sogaz.site.paymentService.enums

enum class PrevStatusEnum(
    val value: String,
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
