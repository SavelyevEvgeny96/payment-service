package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.enums.BankChoosingTypeEnum
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.BankRepository

class BankDaoImpl(
    private val bankRepository: BankRepository,
    private val configDataDao: ConfigDataDao,
) : BankDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val BANK_PRIORITY_CHECK = "bankPriorityCheck"
        const val BANK_PRIORITY = "bankPriority"
        const val BANK_RESERVE = "bankReserve"
        const val TRUE = "true"

        const val LOG_RESOLVE_BANK_ERROR = "При получении банка из конфигурации произошла ошибка: данный банк не поддерживается %s"
        const val LOG_INFO_BANK_PRIORITY_TRUE =
            "Выставлен параметр bankPriority \"true\"." +
                "Все операции будут проводиться по банку:  "
        const val LOG_BANK_ID_IS_NULL_SELECTED_BANK_RESERVE = "Параметр bank_id = null оплата по резервному банку: "
    }

    override fun getBank(
        bankId: String?,
        traceId: String,
        checkBankPriority: String?,
    ): Bank {
        val bank: Bank?
        if (checkBankPriority == TRUE) {
            val priorityBankName = configDataDao.getBankInfoFromConfigData(traceId, BANK_PRIORITY)
            bank = bankRepository.findByBankId(priorityBankName)
            logger.info("$LOG_INFO_BANK_PRIORITY_TRUE ${bank.bankName}")
        } else if (bankId.isNullOrBlank()) {
            val reserveBankName = configDataDao.getBankInfoFromConfigData(traceId, BANK_RESERVE)
            bank = bankRepository.findByBankId(reserveBankName)
            logger.info("$LOG_BANK_ID_IS_NULL_SELECTED_BANK_RESERVE ${bank.bankName}")
        } else {
            bank = bankRepository.findByBankId(bankId)
        }
        return bank
    }

    @Throws(InnerException::class)
    override fun resolveBank(currentBank: BankEnum?): BankEnum =
        when {
            getConfigData(BANK_PRIORITY_CHECK) == TRUE -> findPriorityBank()
            currentBank == null -> findReserveBank()
            else -> currentBank
        }

    private fun findPriorityBank(): BankEnum =
        findBankByChoosingType(BankChoosingTypeEnum.PRIORITY)
            .also { logger.info("$LOG_INFO_BANK_PRIORITY_TRUE $it") }

    private fun findReserveBank(): BankEnum =
        findBankByChoosingType(BankChoosingTypeEnum.RESERVE)
            .also { logger.info("$LOG_BANK_ID_IS_NULL_SELECTED_BANK_RESERVE $it") }

    private fun findBankByChoosingType(choosingType: BankChoosingTypeEnum): BankEnum =
        getConfigData(choosingType.value)
            .run(::convertBankToEnumOrThrow)

    private fun convertBankToEnumOrThrow(bankName: String?): BankEnum =
        BankEnum.from(bankName) ?: throw InnerException(getTraceId(), LOG_RESOLVE_BANK_ERROR.format(bankName))

    private fun getConfigData(name: String): String = configDataDao.getBankInfoFromConfigData(getTraceId(), name)

    override fun findByBankId(bankId: String?): Bank = bankRepository.findByBankId(bankId)
}
