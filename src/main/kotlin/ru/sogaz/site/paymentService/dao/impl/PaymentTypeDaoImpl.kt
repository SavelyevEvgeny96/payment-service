package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.PaymentTypeDao
import ru.sogaz.site.paymentService.entity.PaymentType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository

@Repository
class PaymentTypeDaoImpl(
    private val paymentTypeRepository: PaymentTypeRepository,
) : PaymentTypeDao {
    companion object {
        const val LOG_AND_ERROR_GET_TYPE_STATUS = "Ошибка при получении статуса типа оплаты из таблицы payment_type"
    }

    private val logger = loggerFor(javaClass)

    override fun getPaymentType(
        traceId: String,
        type: String,
    ): PaymentType =
        try {
            paymentTypeRepository.findByTypeId(type).get()
        } catch (e: Exception) {
            logger.error(LOG_AND_ERROR_GET_TYPE_STATUS, e)
            throw InnerException(traceId, LOG_AND_ERROR_GET_TYPE_STATUS)
        }

    override fun findByTypeId(typeId: String) = paymentTypeRepository.findByTypeId(typeId)
}
