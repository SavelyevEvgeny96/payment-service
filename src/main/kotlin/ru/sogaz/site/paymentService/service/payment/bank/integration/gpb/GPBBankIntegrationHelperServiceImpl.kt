package ru.sogaz.site.paymentService.service.payment.bank.integration.gpb

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.DescriptionInfo
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationHelperServiceImpl
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class GPBBankIntegrationHelperServiceImpl(
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
) : BankIntegrationHelperServiceImpl() {
    companion object {
        private const val PAY_ONE_CONTRACT_INFO = "Оплата по договору %s от %s. Платежный сервис, дата операции %s"
        private const val PAY_EMPTY_INFO = "Нет основного договора, дата операции %s"

        private val DEFAULT_ZONE: ZoneId = ZoneId.systemDefault()
        private val DDMMYYYY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    override fun makeDescription(order: Order): DescriptionInfo =
        buildDescriptionInfo(order)

    private fun buildDescriptionInfo(order: Order, maxLen: Int = 160): DescriptionInfo {
        val opDate = LocalDate.now(DEFAULT_ZONE).toContractDateFormat()

        // 1) params — всегда по всем саб-ордерам
        val params: Map<String, String> =
            order.subOrders.mapIndexed { idx, sub -> "param${idx + 1}" to sub.toParamValue() }.toMap()

        // 2) описание сразу с лимитом
        val description: String = when {
            // 0 саб-ордеров
            order.subOrders.isEmpty() -> PAY_EMPTY_INFO.format(opDate).take(maxLen)

            // 1 саб-ордер
            order.subOrders.size == 1 -> order.subOrders.first()
                .makeDescriptionForOneContract(opDate).take(maxLen)

            else -> {
                // если есть "главный" — показываем только его (как и раньше)
                val main = order.subOrders.findLast(SubOrder::mainContractCheck)
                if (main != null) {
                    main.makeDescriptionForOneContract(opDate).take(maxLen)
                } else {
                    // без главного — добавляем договоры по одному, пока влезают
                    val basePrefix = "Оплата "
                    val baseSuffix = ". Платежный сервис, дата операции $opDate"
                    val parts = order.subOrders.map { sub -> "по договору ${sub.toParamValue()}" }

                    // начинаем с первого; если даже он не влез — жёстко обрежем candidate
                    var acc = StringBuilder(parts.first())
                    var candidate = basePrefix + acc.toString() + baseSuffix
                    if (candidate.length > maxLen) return DescriptionInfo(candidate.take(maxLen), params)

                    for (i in 1 until parts.size) {
                        val tryAcc = StringBuilder(acc).append(", ").append(parts[i]).toString()
                        val tryCandidate = basePrefix + tryAcc + baseSuffix
                        if (tryCandidate.length <= maxLen) {
                            acc = StringBuilder(tryAcc)
                            candidate = tryCandidate
                        } else {
                            break
                        }
                    }
                    candidate
                }
            }
        }

        return DescriptionInfo(description = description, params = params)
    }

    private fun SubOrder.makeDescriptionForOneContract(opDate: String): String =
        PAY_ONE_CONTRACT_INFO.format(
            contractId ?: "",
            contractDate?.toContractDateFormat() ?: "",
            opDate,
        )

    private fun Instant.toContractDateFormat(): String =
        this
            .atZone(DEFAULT_ZONE)
            .toLocalDate()
            .toContractDateFormat()

    private fun LocalDate.toContractDateFormat(): String = this.format(DDMMYYYY)
    private fun SubOrder.toParamValue(): String =
        "${contractId ?: ""} от ${contractDate?.toContractDateFormat() ?: ""}"

    fun convertToBankPaymentDetails(response: GpbCardPaymentStatusResponse): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(response)

    fun convertToBankPaymentDetails(response: GpbSbpPaymentStatusResponse): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(response.result.firstOrNull())
}
