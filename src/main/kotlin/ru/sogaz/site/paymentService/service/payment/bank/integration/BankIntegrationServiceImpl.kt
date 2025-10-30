package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.service.BankIntegrationService
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class BankIntegrationServiceImpl : BankIntegrationService {
    companion object {
        const val ERROR_UNKNOWN_PAYMENT_TYPE = "Unknown payment type"
        const val TEMPLATE_VERSION = "01"
        const val QR_TTL = "60"
        const val QR_TYPE = "02"

        private const val ZERO = "0"
        private const val MAX_DESC_LEN = 250

        private const val PREFIX = "Зачисление по операции оплата банковской картой "
        private const val PROVIDER_GPB = "ГПБ"
        private const val PROVIDER_AKB = "АБР"

        private const val TEXT_DOGOVOROV = "договор(ов) "
        private const val TEXT_OT = " от "
        private const val SEP = ", "
        private const val SPACE = " "
        private const val TAIL = " платежный сервис, дата операции "

        private val DDMMYYYY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val DEFAULT_ZONE: ZoneId = ZoneId.systemDefault()
    }

    protected val jsonHeaders = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

    override fun registerPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams?,
    ): Payment =
        when (payment.type) {
            PaymentTypeEnum.CARD -> registerCardPayment(payment)
            PaymentTypeEnum.SBP -> registerSBPPayment(payment, headersParams)
            else -> throw InnerException(getTraceId(), ERROR_UNKNOWN_PAYMENT_TYPE)
        }

    abstract fun registerCardPayment(payment: Payment): Payment

    abstract fun registerSBPPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams?,
    ): Payment

    protected abstract fun provider(): BankEnum

    abstract override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse

    private fun generateDescriptionV4(
        provider: BankEnum,
        subOrders: List<SubOrder>?,
        operationDate: LocalDate,
    ): String {
        val providerText =
            when (provider) {
                BankEnum.GPB -> PROVIDER_GPB
                BankEnum.AKB_RUS -> PROVIDER_AKB
            }

        val blocks =
            subOrders
                .orEmpty()
                .mapNotNull { so ->
                    val parts = mutableListOf<String>()

                    if (isValid(so.contractId)) {
                        val id = so.contractId!!.trim()
                        val datePart =
                            so.contractDate
                                ?.atZone(DEFAULT_ZONE)
                                ?.toLocalDate()
                                ?.format(DDMMYYYY)
                                ?.let { TEXT_OT + it }
                                ?: ""
                        parts += (id + datePart)
                    }

                    if (isValid(so.policyNumber)) {
                        parts += so.policyNumber!!.trim()
                    }

                    val block = parts.joinToString(SPACE)
                    if (block.isNotBlank()){
                        block
                    } else {
                        null
                    }
                }

        val sb =
            StringBuilder(PREFIX)
                .append(providerText)
                .append(SPACE)
                .append(TEXT_DOGOVOROV)

        if (blocks.isNotEmpty()) {
            sb.append(blocks.joinToString(SEP))
        }

        sb
            .append(TAIL)
            .append(operationDate.format(DDMMYYYY))

        val result = sb.toString()
        return if (result.length <= MAX_DESC_LEN){
            result
        } else {
            result.take(MAX_DESC_LEN)
        }
    }

    private fun isValid(v: String?): Boolean = !v.isNullOrBlank() && v.trim() != ZERO

    protected fun Payment.getAmountData() =
        AmountData(
            amount = this.order!!.premiumAmount.toBigDecimal(),
            currency = CurrencyEnum.RUB,
        )

    protected fun Payment.v4Description(operationDate: LocalDate = LocalDate.now(DEFAULT_ZONE)): String =
        generateDescriptionV4(provider(), this.order?.subOrders, operationDate)
}
