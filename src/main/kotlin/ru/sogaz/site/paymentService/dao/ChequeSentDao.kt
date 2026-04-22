package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.ChequeSent

interface ChequeSentDao {
    fun findByPaymentBankId(paymentBankId: String): ChequeSent?

    fun save(chequeSent: ChequeSent): ChequeSent
}
