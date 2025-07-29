package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentRepository

class GetPaymentDaoImpl(
    private val paymentRepository: PaymentRepository,
) : GetPaymentDao {
    companion object {
        const val LOG_ERROR_GET_PAYMENT_BY_ORDER_ID = "Не найден платеж по данному TraceId: "
        const val ERROR_GET_PAYMENT_BY_ORDER_ID = "Не найден платеж по данному order_id: "
    }

    private val logger = loggerFor(javaClass)

    override fun getPayment(
        traceId: String,
        order: Order,
    ): Payment? =
        try {
            paymentRepository.findByOrderId(order)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_GET_PAYMENT_BY_ORDER_ID, traceId)
            throw InnerException(traceId, "$ERROR_GET_PAYMENT_BY_ORDER_ID$order")
        }
}
