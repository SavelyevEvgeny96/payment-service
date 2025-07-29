package ru.sogaz.site.paymentService.util

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_SUCCESS
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.STATUS_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.STATUS_OVERDUE
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.STATUS_SUCCESS
import java.util.Locale
import java.util.UUID

class Util(
    private val configDataRepository: ConfigDataRepository,
) {
    companion object {
        const val DESC_POLICY_NUMBER = "Номера полиса №"
        const val SEPARATOR = ", №"
        const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        const val DESC = "Оплата: "
        const val CODE_LENGTH = "codeLength"
        const val LOG_CODE_LENGTH_NOT_FOUND = "Не найдено значение длины для генерации Order.code "
        const val ERROR_ORDER_CODE_LENGTH_NOT_FOUND = "Длина кода не найдена"
    }

    private val logger = loggerFor(javaClass)

    fun checkStatusOrder(
        orderStatus: OrderStatus?,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
        traceId: String,
    ) {
        if (orderStatus != null) {
            if (orderStatus.stateId == STATUS_SUCCESS) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_SUCCESS + traceId)
                throw BusinessException(errorCodeIsPaidFor, traceId)
            }
            if (orderStatus.stateId == STATUS_OVERDUE || orderStatus.stateId == STATUS_MARKEDDEL) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL + traceId)
                throw BusinessException(errorCodeIsNotAvailable, traceId)
            }
        }
    }

    private fun getCodeLength(traceId: String): Int {
        val config =
            try {
                configDataRepository.findByParamName(CODE_LENGTH)
            } catch (e: Exception) {
                logger.error(e, LOG_CODE_LENGTH_NOT_FOUND)
                throw InnerException(traceId, ERROR_ORDER_CODE_LENGTH_NOT_FOUND)
            }
        return config.paramValue.toIntOrNull() ?: 6
    }

    fun generateUniquePaymentCode(traceId: String): String {
        val codeLength = getCodeLength(traceId)

        return UUID
            .randomUUID()
            .toString()
            .replace("-", "")
            .take(codeLength)
            .uppercase(Locale.getDefault())
    }

    fun generateDescription(sabOrderList: List<SubOrder>): String {
        val policyNumbers =
            sabOrderList
                .mapNotNull { it.policyNumber }
                .filter { it.isNotBlank() && it != "0" }

        val contractIds =
            sabOrderList
                .map { it.contractId }
                .filter { it.isNotBlank() && it != "0" }

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

    fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
}
