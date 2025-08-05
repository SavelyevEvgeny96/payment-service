package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository

class BankDaoImpl(
    private val bankRepository: BankRepository,
    private val configDataRepository: ConfigDataRepository,
) : BankDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val BANK_PRIORITY = "bankPriority"
        const val LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND = "Не найдено значение bankPriority"
    }

    override fun getBank(
        bankId: String?,
        traceId: String,
    ): Bank {
        val bank =
            if (bankId.isNullOrBlank()) {
                val reserveConfigBank = getBankPriority(traceId)
                bankRepository.findByBankId(reserveConfigBank)
            } else {
                bankRepository.findByBankId(bankId)
            }
        return bank
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
