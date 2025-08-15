package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.LOG_ORDER_STATUS_NOT_FOUND
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_NOT_FOUND_ORDER_TO_CODE

class OrderDaoImpl(
    private val orderRepository: OrderRepository,
) : OrderDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val ERROR_ORDER_STATUS_NOT_FOUND = "Статус заказа не найден для stateId 0"
        const val LOG_ERROR_ORDER_SAVE = "Не удалось сохранить данные по заказу"
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

    override fun getOrderId(orderId: String): Order? =
        try {
            orderRepository.findByOrderId(orderId)
        } catch (e: Exception) {
            logger.error(e, LOG_ORDER_STATUS_NOT_FOUND, getTraceId())
            throw InnerException(getTraceId(), ERROR_ORDER_STATUS_NOT_FOUND)
        }

    override fun save(order: Order) {
        try {
            orderRepository.save(order)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_ORDER_SAVE)
            throw InnerException(getTraceId(), LOG_ERROR_ORDER_SAVE + e.message)
        }
    }
}
