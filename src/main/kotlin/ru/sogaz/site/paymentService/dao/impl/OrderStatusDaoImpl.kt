package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderStatusRepository

class OrderStatusDaoImpl(
    private val orderStatusRepository: OrderStatusRepository,
) : OrderStatusDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_AND_ERROR_GET_ORDER_STATUS = "Не удалось найти статус заказа"
    }

    override fun getOrderStatus(
        traceId: String,
        status: String,
    ): OrderStatus =
        try {
            orderStatusRepository.findByStateId(status)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_GET_ORDER_STATUS, traceId)
            throw InnerException(traceId, LOG_AND_ERROR_GET_ORDER_STATUS)
        }
}
