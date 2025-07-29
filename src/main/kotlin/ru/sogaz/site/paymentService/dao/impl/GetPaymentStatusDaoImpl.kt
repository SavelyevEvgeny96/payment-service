package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_GET_PAYMENT_STATUS

class GetPaymentStatusDaoImpl(
    private val paymentStatusRepository: PaymentStatusRepository,
) : GetPaymentStatusDao {
    companion object {
    }

    private val logger = loggerFor(javaClass)

    override fun getPaymentStatus(
        traceId: String,
        status: String,
    ): PaymentStatus =
        try {
            paymentStatusRepository.findByStateId(status)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_GET_PAYMENT_STATUS, traceId)
            throw InnerException(traceId, LOG_AND_ERROR_GET_PAYMENT_STATUS)
        }
}
