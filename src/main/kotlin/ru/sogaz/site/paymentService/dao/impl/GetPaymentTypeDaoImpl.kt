package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.entity.PaymentType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_GET_TYPE_STATUS

class GetPaymentTypeDaoImpl(private val paymentTypeRepository: PaymentTypeRepository) : GetPaymentTypeDao {
    private val logger = loggerFor(javaClass)
    override fun getPaymentType(traceId: String, type: String): PaymentType {
        return try {
            paymentTypeRepository.findByTypeId(type)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_GET_TYPE_STATUS, traceId)
            throw InnerException(traceId, LOG_AND_ERROR_GET_TYPE_STATUS)
        }
    }
}