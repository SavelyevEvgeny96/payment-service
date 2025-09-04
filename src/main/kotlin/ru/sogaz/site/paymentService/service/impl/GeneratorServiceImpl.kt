package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.service.ConfigDataService
import ru.sogaz.site.paymentService.service.GeneratorService
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class GeneratorServiceImpl(
    private val configDataService: ConfigDataService,
) : GeneratorService {
    companion object {
        const val DESC_POLICY_NUMBER = "Номера полиса №"
        const val SEPARATOR = ", №"
        const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        const val DESC = "Оплата: "
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    }

    override fun generateDescription(sabOrderList: List<SubOrder>?): String {
        val policyNumbers =
            sabOrderList
                ?.mapNotNull { it.policyNumber }
                ?.filter { it.isNotBlank() && it != "0" }

        val contractIds =
            sabOrderList
                ?.map { it.contractId }
                ?.filter { it?.isNotBlank() == true && it != "0" }

        val description =
            buildString {
                append(DESC)

                if (policyNumbers?.isNotEmpty() == true || contractIds?.isNotEmpty() == true) {
                    append(" (")

                    if (policyNumbers?.isNotEmpty() == true) {
                        append(DESC_POLICY_NUMBER)
                        append(policyNumbers.joinToString(SEPARATOR))
                    }

                    if (contractIds?.isNotEmpty() == true) {
                        if (policyNumbers?.isNotEmpty() == true) append("; ")
                        append(DESC_INSURANCE_CONTRACT)
                        append(contractIds.joinToString(SEPARATOR))
                    }

                    append(")")
                }
            }
        return description
    }

    override fun getDescriptionAndPremiumAmount(
        premiumAmount: String?,
        listSubOrder: List<SubOrder>?,
    ): DataDescriptionAndPremiumAmount =
        DataDescriptionAndPremiumAmount(
            premiumAmount?.replace(".", ""),
            generateDescription(listSubOrder),
        )

    override fun nowPlusFormatted(days: Long, minutes: Long): String =
        LocalDateTime.now().plusDays(days).plusMinutes(minutes).format(formatter)

    override fun formatDuration(duration: Duration): String {
        val totalSeconds = duration.toMillis() / 1000.0
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes}m${"%.1f".format(seconds)}s"
        } else {
            "%.1fs".format(seconds)
        }
    }

    override fun jsonHeaders(): HttpHeaders = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }

    override fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
}
