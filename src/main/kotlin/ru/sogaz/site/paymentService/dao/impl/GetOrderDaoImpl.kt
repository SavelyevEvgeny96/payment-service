package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.paymentService.dao.GetOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_NOT_FOUND_ORDER_TO_CODE

class GetOrderDaoImpl(private val orderRepository: OrderRepository) : GetOrderDao {
    private val logger = loggerFor(javaClass)
    override fun getOrderByCode(code: String, traceId: String): Order {
        return try {
            orderRepository.findByCode(code)
        } catch (e: Exception) {
            logger.error(e, LOG_NOT_FOUND_ORDER_TO_CODE, code, traceId)
            throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND, traceId)
        }
    }
}