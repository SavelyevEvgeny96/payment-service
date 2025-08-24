package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_FIND_SUB_ORDER

class SubOrderDaoImpl(
    private val subOrderRepository: SubOrderRepository,
) : SubOrderDao {
    private val logger = loggerFor(javaClass)

    override fun getSubOrder(
        traceId: String,
        order: Order?,
    ): SubOrder =
        try {
            subOrderRepository.findFirstByOrderId(order)
        } catch (e: Exception) {
            logger.error("$LOG_AND_ERROR_FIND_SUB_ORDER ${order?.orderId}")
            throw InnerException(traceId, "$LOG_AND_ERROR_FIND_SUB_ORDER${order?.orderId}")
        }

    override fun getAllSubOrderListByOrderId(orderId: Order, traceId: String): List<SubOrder> =
        try {
            subOrderRepository.findAllByOrderId(orderId)
        } catch (ex: Exception) {
            logger.error("$LOG_AND_ERROR_FIND_SUB_ORDER ${orderId.orderId}")
            throw InnerException(traceId, "$LOG_AND_ERROR_FIND_SUB_ORDER${orderId.orderId}")
        }

}
