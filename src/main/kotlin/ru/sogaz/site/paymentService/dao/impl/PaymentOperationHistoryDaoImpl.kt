package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.GET_TOKEN_MASSAGE_FAIL

class PaymentOperationHistoryDaoImpl(
    private val paymentOperationHistoryRepository: PaymentOperationHistoryRepository,
    private val getActionTypeDao: GetActionTypeDao
) : PaymentOperationHistoryDao {
    companion object {
        const val LOG_ERROR_PAYMENT_HISTORY_SAVE = "Не удалось сохранить историю "
    }

    private val logger = loggerFor(javaClass)

    override fun saveRecordOperationHistory(order:Order?, clientSystem: ClientSystem?, traceId:String, actionTypeName:String) {
        try {
            val actionType= getActionTypeDao.getActionType(traceId, actionTypeName)
            val operationHistory =
                PaymentOperationHistory(
                    action = actionType,
                    order = order,
                    actionAuthor = clientSystem
                )
            paymentOperationHistoryRepository.save(operationHistory)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_PAYMENT_HISTORY_SAVE)
            throw InnerException(RequestInfo.getTraceId(), "$LOG_ERROR_PAYMENT_HISTORY_SAVE ${e.message}")
        }
    }
}
