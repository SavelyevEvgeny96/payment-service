package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.service.BankIntegrationService
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class BankIntegrationServiceImpl : BankIntegrationService {
    companion object {
        const val ERROR_UNKNOWN_PAYMENT_TYPE = "Unknown payment type"
        const val TEMPLATE_VERSION = "01"
        const val QR_TTL = "60"
        const val QR_TYPE = "02"
        const val RUB = "RUB"

        private const val DESC_POLICY_NUMBER = "Номера полиса №"
        private const val SEPARATOR = ", №"
        private const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        private const val DESC = "Оплата: "

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun jsonHeaders() = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        fun nowPlusFormatted(
            days: Long = 0,
            minutes: Long = 0,
        ): String =
            LocalDateTime
                .now()
                .plusDays(days)
                .plusMinutes(minutes)
                .format(formatter)

        fun formatDuration(duration: Duration): String {
            val totalSeconds = duration.toMillis() / 1000.0
            val minutes = (totalSeconds / 60).toInt()
            val seconds = totalSeconds % 60
            return if (minutes > 0) {
                "${minutes}m${"%.1f".format(seconds)}s"
            } else {
                "%.1fs".format(seconds)
            }
        }
    }

    override fun registerPayment(payment: Payment): Payment =
        when (payment.type) {
            PaymentTypeEnum.CARD -> registerCardPayment(payment)
            PaymentTypeEnum.SBP -> registerSBPPayment(payment)
            else -> throw InnerException(getTraceId(), ERROR_UNKNOWN_PAYMENT_TYPE)
        }

    abstract fun registerCardPayment(payment: Payment): Payment

    abstract fun registerSBPPayment(payment: Payment): Payment

    abstract override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse

    fun generateDescription(
        premiumAmount: String?,
        listSubOrder: List<SubOrder>?,
    ): DataDescriptionAndPremiumAmount =
        DataDescriptionAndPremiumAmount(
            premiumAmount?.replace(".", ""),
            generateDescription(listSubOrder),
        )

    private fun generateDescription(sabOrderList: List<SubOrder>?): String {
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
}
