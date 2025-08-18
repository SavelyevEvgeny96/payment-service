package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.LOG_ORDER_STATUS_NOT_FOUND

class OrderDaoImpl(
    private val orderRepository: OrderRepository,
) : OrderDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val ERROR_ORDER_STATUS_NOT_FOUND = "Статус заказа не найден для stateId 0"
        const val LOG_ERROR_ORDER_SAVE = "Не удалось сохранить данные по заказу"
    }

    override fun getOrderId(
        traceId: String,
        orderId: String,
    ): Order =
        try {
            orderRepository.findByOrderId(orderId)
        } catch (e: Exception) {
            logger.error(e, LOG_ORDER_STATUS_NOT_FOUND, traceId)
            throw InnerException(traceId, ERROR_ORDER_STATUS_NOT_FOUND)
        }

    override fun save(order: Order) {
        try {
            orderRepository.save(order)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_ORDER_SAVE)
            throw InnerException(RequestInfo.getTraceId(), LOG_ERROR_ORDER_SAVE + e.message)
        }
    }
}
