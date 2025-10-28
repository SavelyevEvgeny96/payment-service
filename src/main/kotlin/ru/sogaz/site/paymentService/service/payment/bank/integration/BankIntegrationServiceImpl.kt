package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.service.BankIntegrationService

abstract class BankIntegrationServiceImpl : BankIntegrationService {
    companion object {
        const val ERROR_UNKNOWN_PAYMENT_TYPE = "Unknown payment type"
        const val TEMPLATE_VERSION = "01"
        const val QR_TTL = "60"
        const val QR_TYPE = "02"

        private const val DESC_POLICY_NUMBER = "Номера полиса №"
        private const val SEPARATOR = ", №"
        private const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        private const val DESC = "Оплата: "
    }

    protected val jsonHeaders = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

    override fun registerPayment(payment: Payment): Payment =
        when (payment.type) {
            PaymentTypeEnum.CARD -> registerCardPayment(payment)
            PaymentTypeEnum.SBP -> registerSBPPayment(payment)
            else -> throw InnerException(getTraceId(), ERROR_UNKNOWN_PAYMENT_TYPE)
        }

    abstract fun registerCardPayment(payment: Payment): Payment

    abstract fun registerSBPPayment(payment: Payment): Payment

    abstract override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse

    private fun generateDescription(subOrders: List<SubOrder>): String {
        val policyNumbers = collectPolicyNumbers(subOrders)

        val contractIds = collectContractIds(subOrders)

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

    private fun collectPolicyNumbers(subOrders: List<SubOrder>): List<String> =
        subOrders
            .mapNotNull { it.policyNumber }
            .filter(::numberFilter)

    private fun collectContractIds(subOrders: List<SubOrder>): List<String> =
        subOrders
            .mapNotNull { it.contractId }
            .filter(::numberFilter)

    private fun numberFilter(number: String) = number.isNotBlank() && number != "0"

    protected fun Payment.getAmountData() =
        AmountData(
            amount = this.order!!.premiumAmount.toBigDecimal(),
            currency = CurrencyEnum.RUB,
        )

    protected fun Payment.getDescription() = generateDescription(this.order!!.subOrders)
}
