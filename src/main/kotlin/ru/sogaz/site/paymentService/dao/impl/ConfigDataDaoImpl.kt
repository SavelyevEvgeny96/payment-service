package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository

class ConfigDataDaoImpl(
    private val configDataRepository: ConfigDataRepository,
) : ConfigDataDao {
    companion object {
        const val LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND = "Не найдено значение : "
    }

    private val logger = loggerFor(javaClass)

    override fun getBankInfoFromConfigData(
        traceId: String,
        valueNameInfo: String,
    ): String {
        val config =
            try {
                configDataRepository.findByParamName(valueNameInfo)
            } catch (e: Exception) {
                logger.error("$LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND $valueNameInfo")
                throw InnerException(traceId, "$LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND$valueNameInfo")
            }
        return config.paramValue
    }
}
