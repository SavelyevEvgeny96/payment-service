package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.SubOrderRepository

class SubOrderDaoImpl(
    private val subOrderRepository: SubOrderRepository,
) : SubOrderDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_ERROR_SUB_ORDER_SAVE = "Не удалось сохранить данные по подзаказу"
        const val LOG_AND_ERROR_FIND_SUB_ORDER = "Ошибка получения SubOrder c orderId:  "
    }

    override fun getSubOrder(
        traceId: String,
        order: Order?,
    ): SubOrder =
        try {
            subOrderRepository.findFirstByOrderId(order)
        } catch (e: Exception) {
            logger.error("$LOG_AND_ERROR_FIND_SUB_ORDER ${order?.id}")
            throw InnerException(traceId, "$LOG_AND_ERROR_FIND_SUB_ORDER${order?.id}")
        }

    override fun save(subOrder: SubOrder) {
        try {
            subOrderRepository.save(subOrder)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_SUB_ORDER_SAVE)
            throw InnerException(getTraceId(), LOG_ERROR_SUB_ORDER_SAVE + e.message)
        }
    }

    override fun saveAll(subOrders: Iterable<SubOrder>): List<SubOrder> = subOrderRepository.saveAll(subOrders)

    override fun getAllSubOrderListByOrderId(
        orderId: Order,
        traceId: String,
    ): List<SubOrder> =
        try {
            subOrderRepository.findAllByOrderId(orderId)
        } catch (ex: Exception) {
            logger.error("$LOG_AND_ERROR_FIND_SUB_ORDER ${orderId.id}")
            throw InnerException(traceId, "$LOG_AND_ERROR_FIND_SUB_ORDER${orderId.id}")
        }
}
