package ru.sogaz.site.paymentService.util

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_SUCCESS

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
        val status = StatusEnum.fromValue(orderStatus?.stateId)
        if (status != null) {
            when {
                status.isPaidFor() -> {
                    logger.error("${orderStatus?.stateId} $LOG_ORDER_STATUS_SUCCESS $traceId")
                    throw BusinessException(errorCodeIsPaidFor, traceId)
                }
                status.isNotAvailable() -> {
                    logger.error("${orderStatus?.stateId} $LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL $traceId")
                    throw BusinessException(errorCodeIsNotAvailable, traceId)
                }
                else -> {
                }
            }
        }
    }

    fun getCodeLength(traceId: String): Int {
        val config =
            try {
                configDataRepository.findByParamName(CODE_LENGTH)
            } catch (e: Exception) {
                logger.error(e, LOG_CODE_LENGTH_NOT_FOUND)
                throw InnerException(traceId, ERROR_ORDER_CODE_LENGTH_NOT_FOUND)
            }
        return config.paramValue.toIntOrNull() ?: 6
    }
}
