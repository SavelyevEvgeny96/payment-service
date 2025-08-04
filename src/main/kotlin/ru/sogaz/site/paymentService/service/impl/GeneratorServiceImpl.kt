package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.util.Util
import ru.sogaz.site.paymentService.util.Util.Companion.DESC
import ru.sogaz.site.paymentService.util.Util.Companion.DESC_INSURANCE_CONTRACT
import ru.sogaz.site.paymentService.util.Util.Companion.DESC_POLICY_NUMBER
import ru.sogaz.site.paymentService.util.Util.Companion.SEPARATOR
import java.util.Locale
import java.util.UUID

class GeneratorServiceImpl(
    private val util: Util,
) : GeneratorService {
    override fun generateUniquePaymentCode(traceId: String): String {
        val codeLength = util.getCodeLength(traceId)

        return UUID
            .randomUUID()
            .toString()
            .replace("-", "")
            .take(codeLength)
            .uppercase(Locale.getDefault())
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
}
