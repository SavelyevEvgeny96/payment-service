package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_FIND_SUB_ORDER

class GetSubOrderDaoImpl(private val subOrderRepository: SubOrderRepository): GetSubOrderDao {
    private val logger = loggerFor(javaClass)
   override fun getSubOrder(traceId: String, order: Order): SubOrder {
        return try {
            subOrderRepository.findFirstByOrderId(order)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_FIND_SUB_ORDER, order.code)
            throw InnerException(traceId, "$LOG_AND_ERROR_FIND_SUB_ORDER$order")
        }
    }
}