package ru.sogaz.site.paymentService.enums

import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank

enum class BankEnum(
    @JsonValue val code: String,
    val description: String,
) {
    GPB("gpb", "ГПБ"),
    ABR("abr", "АБР"),
    ;

    fun toOperationBank(): OperationBank =
        when (this) {
            GPB -> OperationBank.GPB
            ABR -> OperationBank.ABR
        }

    companion object {
        private const val LEGACY_ABR_CODE = "abr_rus"

        fun from(value: String?): BankEnum? =
            BankEnum.entries.find { it.code == value || it.name == value }
                ?: ABR.takeIf { value == LEGACY_ABR_CODE }
    }
}
