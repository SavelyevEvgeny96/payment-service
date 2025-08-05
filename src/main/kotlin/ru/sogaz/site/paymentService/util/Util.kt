package ru.sogaz.site.paymentService.util

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository

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
