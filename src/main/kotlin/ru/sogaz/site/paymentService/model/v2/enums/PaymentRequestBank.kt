package ru.sogaz.site.paymentService.model.v2.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class PaymentRequestBank(
    @JsonValue val value: String,
) {
    GPB("GPB"),
    ABR("ABR"),
    ;

    fun toOperationBank(): OperationBank =
        when (this) {
            GPB -> OperationBank.GPB
            ABR -> OperationBank.ABR
        }
}
