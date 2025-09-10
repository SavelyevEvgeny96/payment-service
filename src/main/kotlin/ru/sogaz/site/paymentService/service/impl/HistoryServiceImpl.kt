package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.HistoryService
import java.time.LocalDateTime

class HistoryServiceImpl(
    private val subOrderDao: SubOrderDao,
    private val operationHistoryDao: PaymentOperationHistoryDao,
) : HistoryService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_OPERATION_HISTORY_ADDED =
            "Запись о проверке статуса добавлена в историю для заказа %s. TraceId: %s"
    }

    override fun createOrderHistoryRecord(
        order: Order,
        traceId: String,
    ) {
        val actionType = ActionType.ORDER_PAID.value
        val subOrder = subOrderDao.getAllSubOrderListByOrderId(order, traceId)

        val historyRecord =
            PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.first().clientSystem,
                actionDate = LocalDateTime.now(),
            )

        historyRecord.action?.let {
            operationHistoryDao.saveRecordOperationHistory(
                historyRecord.order,
                historyRecord.actionAuthor,
                traceId,
                it,
            )
        }
        logger.info(LOG_OPERATION_HISTORY_ADDED.format(order.orderId, traceId))
    }
}
