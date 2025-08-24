package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.ActionTypeDao
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_FIND_ACTION_TYPE

class ActionTypeDaoImpl(
    private val actionTypeRepository: ActionTypeRepository,
) : ActionTypeDao {
    private val logger = loggerFor(javaClass)

    override fun getActionType(
        traceId: String,
        actionType: String,
    ): ActionType =
        try {
            actionTypeRepository.findByActionName(actionType)
        } catch (e: Exception) {
            logger.error(e, LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
            throw InnerException(traceId, LOG_AND_ERROR_FIND_ACTION_TYPE)
        }
}
