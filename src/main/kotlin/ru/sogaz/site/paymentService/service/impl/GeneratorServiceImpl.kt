package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.service.ConfigDataService
import ru.sogaz.site.paymentService.service.GeneratorService
import java.util.UUID

class GeneratorServiceImpl(
    private val configDataService: ConfigDataService,
) : GeneratorService {
    companion object {
        const val DESC_POLICY_NUMBER = "Номера полиса №"
        const val SEPARATOR = ", №"
        const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        const val DESC = "Оплата: "
    }

    override fun generateDescription(sabOrderList: List<SubOrder>): String {
        val policyNumbers =
            sabOrderList
                .mapNotNull { it.policyNumber }
                .filter { it.isNotBlank() && it != "0" }

        val contractIds =
            sabOrderList
                .map { it.contractId }
                .filter { it?.isNotBlank() == true && it != "0" }

        val description =
            buildString {
                append(DESC)

                if (policyNumbers.isNotEmpty() || contractIds.isNotEmpty()) {
                    append(" (")

                    if (policyNumbers.isNotEmpty()) {
                        append(DESC_POLICY_NUMBER)
                        append(policyNumbers.joinToString(SEPARATOR))
                    }

                    if (contractIds.isNotEmpty()) {
                        if (policyNumbers.isNotEmpty()) append("; ")
                        append(DESC_INSURANCE_CONTRACT)
                        append(contractIds.joinToString(SEPARATOR))
                    }

                    append(")")
                }
            }
        return description
    }

    override fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
    override fun getDescriptionAndPremiumAmount(
        premiumAmount: String?,
        listSubOrder: List<SubOrder>
    ): DataDescriptionAndPremiumAmount {
        return DataDescriptionAndPremiumAmount(
            premiumAmount?.replace(".", ""),
            generateDescription(listSubOrder)
        )
    }
}
