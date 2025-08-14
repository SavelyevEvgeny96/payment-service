package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository

class CallbackPaymentDaoImpl(
    private val callbackPaymentRepository: CallbackPaymentRepository,
) : CallbackPaymentDao {
    companion object {
        const val LOG_ERROR_CALLBACK_PAYMENT = "Не удалось сохранить данные платежа"
    }

    private val logger = loggerFor(javaClass)

    override fun save(callbackPayment: CallbackPayment) {
        try {
            callbackPaymentRepository.save(callbackPayment)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_CALLBACK_PAYMENT)
            throw InnerException(RequestInfo.getTraceId(), LOG_ERROR_CALLBACK_PAYMENT + e.message)
        }
    }

    override fun findByPaymentBankId(paymentBankId: String): CallbackPayment? =
        try {
            callbackPaymentRepository.findByPaymentBankId(paymentBankId)
        } catch (e: Exception) {
            logger.error(e, "Не удалось найти данные платежа по paymentBankId")
            null
        }
}
