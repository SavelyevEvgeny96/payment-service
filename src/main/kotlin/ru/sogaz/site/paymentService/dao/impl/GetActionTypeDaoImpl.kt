package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_FIND_ACTION_TYPE

class GetActionTypeDaoImpl(private val actionTypeRepository: ActionTypeRepository):GetActionTypeDao {
    private val logger = loggerFor(javaClass)
    override fun getActionType(traceId: String, actionType: String): ActionType {
        return try {
            actionTypeRepository.findByActionName(actionType)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
            throw InnerException(traceId, LOG_AND_ERROR_FIND_ACTION_TYPE)
        }
    }
}