package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetBankPriorityDao
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository

class GetBankPriorityDaoImpl(
    private val configDataRepository: ConfigDataRepository,
) : GetBankPriorityDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val BANK_PRIORITY = "bankPriority"
        const val LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND = "Не найдено значение bankPriority"
    }

    override fun getBankPriority(traceId: String): String {
        val config =
            try {
                configDataRepository.findByParamName(BANK_PRIORITY)
            } catch (e: Exception) {
                logger.error(e, LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND)
                throw InnerException(traceId, LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND)
            }
        return config.paramValue
    }
}
