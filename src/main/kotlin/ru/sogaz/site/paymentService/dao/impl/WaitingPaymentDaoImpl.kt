package ru.sogaz.site.paymentService.dao.impl

import jakarta.transaction.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.PaymentMapper
import ru.sogaz.site.paymentService.repository.WaitingPaymentRepository

@Repository
class WaitingPaymentDaoImpl(
    private val waitingPaymentRepository: WaitingPaymentRepository,
    private val paymentMapper: PaymentMapper,
) : WaitingPaymentDao {
    companion object {
        const val LOG_ERROR_CALLBACK_PAYMENT = "Не удалось найти данные платежа по paymentBankId"
    }

    private val logger = loggerFor(javaClass)

    override fun findByPaymentBankId(paymentBankId: String): WaitingPayment? =
        try {
            waitingPaymentRepository.findByPaymentBankId(paymentBankId)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_CALLBACK_PAYMENT, e)
            throw e
        }

    override fun saveWaitingForPayment(payment: Payment): WaitingPayment =
        payment
            .run(::findWaitingPaymentOrGetNew)
            .run(waitingPaymentRepository::save)

    private fun findWaitingPaymentOrGetNew(payment: Payment): WaitingPayment =
        payment.paymentBankId
            ?.let(::findByPaymentBankId)
            ?: paymentMapper.toWaitingPayment(payment)

    override fun findTopNEarliestUpdated(limit: Int): List<WaitingPayment> =
        limit
            .run(Pageable::ofSize)
            .run(waitingPaymentRepository::findAllByOrderByUpdateDate)

    @Transactional
    override fun deleteByPaymentBankId(paymentBankId: String) = waitingPaymentRepository.deleteByPaymentBankId(paymentBankId)

    @Transactional
    override fun updateTimeByPaymentBankId(paymentBankId: String) = waitingPaymentRepository.updateTimeByPaymentBankId(paymentBankId)
}
