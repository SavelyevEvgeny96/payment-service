package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.PaymentTypeDao
import ru.sogaz.site.paymentService.entity.PaymentType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_GET_TYPE_STATUS

class PaymentTypeDaoImpl(
    private val paymentTypeRepository: PaymentTypeRepository,
) : PaymentTypeDao {
    private val logger = loggerFor(javaClass)

    override fun getPaymentType(
        traceId: String,
        type: String,
    ): PaymentType =
        try {
            paymentTypeRepository.findByTypeId(type)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_GET_TYPE_STATUS, traceId)
            throw InnerException(traceId, LOG_AND_ERROR_GET_TYPE_STATUS)
        }
}
