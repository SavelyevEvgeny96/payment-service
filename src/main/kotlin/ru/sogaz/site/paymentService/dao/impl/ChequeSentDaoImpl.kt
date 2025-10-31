package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.dao.ChequeSentDao
import ru.sogaz.site.paymentService.entity.ChequeSent
import ru.sogaz.site.paymentService.repository.ChequeSentRepository

@Repository
class ChequeSentDaoImpl(
    private val chequeSentRepository: ChequeSentRepository,
) : ChequeSentDao {
    override fun findByPaymentBankId(paymentBankId: String): ChequeSent? = chequeSentRepository.findByPaymentBankId(paymentBankId)

    override fun save(chequeSent: ChequeSent): ChequeSent = chequeSentRepository.save(chequeSent)
}
