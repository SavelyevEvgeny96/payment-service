package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetOrderStatusDao
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.ERROR_ORDER_STATUS_NOT_FOUND
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.LOG_ORDER_STATUS_NOT_FOUND

class GetOrderStatusDaoImpl(
    private val orderStatusRepository: OrderStatusRepository,
) : GetOrderStatusDao {
    private val logger = loggerFor(javaClass)

    override fun getOrderStatus(
        traceId: String,
        stateOrder: String,
    ): OrderStatus =
        try {
            orderStatusRepository.findByStateId(stateOrder)
        } catch (e: Exception) {
            logger.error(e, LOG_ORDER_STATUS_NOT_FOUND, traceId)
            throw InnerException(traceId, ERROR_ORDER_STATUS_NOT_FOUND)
        }
}
