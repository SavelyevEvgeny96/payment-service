package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository

class CallbackPaymentDaoImpl(
    private val callbackPaymentRepository: CallbackPaymentRepository,
) : CallbackPaymentDao {
    companion object {
        const val LOG_ERROR_CALLBACK_PAYMENT = "Не удалось сохранить данные платежа"
    }

    private val logger = loggerFor(javaClass)

    override fun save(callbackPayment: CallbackPayment): CallbackPayment {
        try {
            return callbackPaymentRepository.save(callbackPayment)
        } catch (ex: Exception) {
            logger.error(LOG_ERROR_CALLBACK_PAYMENT, ex)
            throw InnerException(getTraceId(), "$LOG_ERROR_CALLBACK_PAYMENT ${ex.message}")
        }
    }

    override fun findByPaymentBankId(paymentBankId: String): CallbackPayment? =
        try {
            callbackPaymentRepository.findByPaymentBankId(paymentBankId)
        } catch (ex: Exception) {
            logger.error("Не удалось найти данные платежа по paymentBankId", ex)
            throw ex
        }

    override fun saveCallbackForPayment(payment: Payment): CallbackPayment =
        findCallbackPaymentOrGetNew(payment)
            .apply {
                bankId = payment.bank?.code
                typeId = payment.type?.value
                paymentBankId = payment.paymentBankId
            }.run(::save)

    private fun findCallbackPaymentOrGetNew(payment: Payment) = payment.paymentBankId?.let(::findByPaymentBankId) ?: CallbackPayment()
}
