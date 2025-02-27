package ru.sogaz.site.paymentService.dto

import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import java.util.*
@Component
open class ConfigDataDao(private val configDataRepository: ConfigDataRepository) {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_CODE_LENGTH_NOT_FOUND = "Не найдено значение длины для генерации Order.code "
        const val ERROR_ORDER_CODE_LENGTH_NOT_FOUND = "Длина кода не найдена"

    }
    private fun getCodeLength(traceId: String): Int {
        val config =
            try {
                configDataRepository.findByParamName("codeLength")
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

    fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
}


