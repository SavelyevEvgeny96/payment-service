package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.WaitingPaymentRepository

@Repository
class WaitingPaymentDaoImpl(
    private val waitingPaymentRepository: WaitingPaymentRepository,
) : WaitingPaymentDao {
    companion object {
        const val LOG_ERROR_CALLBACK_PAYMENT = "Не удалось сохранить данные платежа"
    }

    private val logger = loggerFor(javaClass)

    override fun save(waitingPayment: WaitingPayment): WaitingPayment =
        try {
            waitingPaymentRepository.save(waitingPayment)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_CALLBACK_PAYMENT, e)
            throw InnerException(RequestInfo.getTraceId(), "$LOG_ERROR_CALLBACK_PAYMENT ${e.message}")
        }

    override fun findByPaymentBankId(paymentBankId: String): WaitingPayment? =
        try {
            waitingPaymentRepository.findByPaymentBankId(paymentBankId)
        } catch (e: Exception) {
            logger.error("Не удалось найти данные платежа по paymentBankId", e)
            throw e
        }

    override fun saveWaitingForPayment(payment: Payment): WaitingPayment =
        findCallbackPaymentOrGetNew(payment)
            .apply {
                bank = payment.bank
                type = PaymentTypeEnum.CARD
                paymentBankId = payment.paymentBankId
            }.run(::save)

    private fun findCallbackPaymentOrGetNew(payment: Payment): WaitingPayment =
        payment.paymentBankId?.let(::findByPaymentBankId) ?: WaitingPayment()
}
