package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.TRUE

class BankDaoImpl(
    private val bankRepository: BankRepository,
    private val configDataDao: ConfigDataDao,
) : BankDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val BANK_PRIORITY = "bankPriority"
        const val BANK_RESERVE = "bankReserve"

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
}
