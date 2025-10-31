package ru.sogaz.site.paymentService.dao.impl

import jakarta.transaction.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.PaymentMapper
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository

@Repository
class CallbackPaymentDaoImpl(
    private val callbackPaymentRepository: CallbackPaymentRepository,
    private val paymentMapper: PaymentMapper,
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

    override fun saveCallbackForPayment(payment: Payment): CallbackPayment =
        payment
            .run(::findCallbackPaymentOrGetNew)
            .run(callbackPaymentRepository::save)

    private fun findCallbackPaymentOrGetNew(payment: Payment): CallbackPayment =
        payment.paymentBankId
            ?.let(::findByPaymentBankId)
            ?: paymentMapper.toCallbackPayment(payment)

    override fun findByPaymentBankId(paymentBankId: String): CallbackPayment? =
        try {
            callbackPaymentRepository.findByPaymentBankId(paymentBankId)
        } catch (ex: Exception) {
            logger.error("Не удалось найти данные платежа по paymentBankId", ex)
            throw ex
        }

    override fun findLimitEarliestUpdated(limit: Int): List<CallbackPayment> =
        limit
            .run(Pageable::ofSize)
            .run(callbackPaymentRepository::findAllByOrderByUpdateDate)

    @Transactional
    override fun deleteByPaymentBankId(paymentBankId: String) = callbackPaymentRepository.deleteByPaymentBankId(paymentBankId)

    @Transactional
    override fun updateTimeByPaymentBankId(paymentBankId: String) = callbackPaymentRepository.updateTimeByPaymentBankId(paymentBankId)
}
