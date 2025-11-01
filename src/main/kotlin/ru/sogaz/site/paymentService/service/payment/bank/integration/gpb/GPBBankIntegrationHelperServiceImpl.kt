package ru.sogaz.site.paymentService.service.payment.bank.integration.gpb

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationHelperServiceImpl
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class GPBBankIntegrationHelperServiceImpl(
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper
) : BankIntegrationHelperServiceImpl() {
    companion object {
        private const val PAY_ONE_CONTRACT_INFO = "Оплата по договору %s от %s. Платежный сервис, дата операции %s"
        private const val PAY_EMPTY_INFO = "Нет основного договора, дата операции %s"

        private val DEFAULT_ZONE: ZoneId = ZoneId.systemDefault()
        private val DDMMYYYY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    override fun makeDescription(order: Order): String =
        when (order.subOrders.size) {
            0 -> PAY_EMPTY_INFO
            1 -> order.subOrders.first().makeDescriptionForOneContract()
            else -> order.subOrders.findMainOrFirstContract().makeDescriptionForOneContract()
        }

    private fun List<SubOrder>.findMainOrFirstContract(): SubOrder = this.findLast(SubOrder::mainContractCheck) ?: this.first()

    private fun SubOrder.makeDescriptionForOneContract(): String =
        PAY_ONE_CONTRACT_INFO.format(
            contractId ?: "",
            contractDate?.toContractDateFormat() ?: "",
            LocalDate.now(DEFAULT_ZONE).toContractDateFormat(),
        )

    private fun Instant.toContractDateFormat(): String =
        this
            .atZone(DEFAULT_ZONE)
            .toLocalDate()
            .toContractDateFormat()

    private fun LocalDate.toContractDateFormat(): String =
        this.format(DDMMYYYY)

    fun convertToBankPaymentDetails(response: GpbCardPaymentStatusResponse): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(response, response.getStatus())

    fun convertToBankPaymentDetails(response: GpbSbpPaymentStatusResponse): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(response, response.getStatus())

    private fun GpbCardPaymentStatusResponse.getStatus(): StatusEnum = result?.status ?: StatusEnum.WAIT

    private fun GpbSbpPaymentStatusResponse.getStatus(): StatusEnum = result.firstOrNull()?.status ?: StatusEnum.WAIT

}
