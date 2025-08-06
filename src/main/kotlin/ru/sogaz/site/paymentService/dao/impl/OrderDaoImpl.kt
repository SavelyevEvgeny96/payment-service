package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.LOG_ORDER_STATUS_NOT_FOUND
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_NOT_FOUND_ORDER_TO_CODE

class OrderDaoImpl(
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
) : OrderDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val ERROR_ORDER_STATUS_NOT_FOUND = "Статус заказа не найден для stateId 0"
    }

    override fun getOrderByCode(
        code: String,
        traceId: String,
    ): Order =
        try {
            orderRepository.findByCode(code)
        } catch (e: Exception) {
            logger.error(e, LOG_NOT_FOUND_ORDER_TO_CODE, code, traceId)
            throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND, traceId)
        }

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
