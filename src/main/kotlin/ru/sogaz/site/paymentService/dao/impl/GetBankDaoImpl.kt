package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.paymentService.dao.GetBankDao
import ru.sogaz.site.paymentService.dao.GetBankPriorityDao
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.repository.BankRepository

class GetBankDaoImpl(
    private val bankRepository: BankRepository,
    private val getBankPriorityDao: GetBankPriorityDao,
) : GetBankDao {
    override fun getBank(
        bankId: String?,
        traceId: String,
    ): Bank? {
        val bank =
            if (bankId.isNullOrBlank()) {
                val reserveConfigBank = getBankPriorityDao.getBankPriority(traceId)
                bankRepository.findByBankId(reserveConfigBank)
            } else {
                bankRepository.findByBankId(bankId)
            }
        return bank
    }
}
