package ru.sogaz.site.paymentService.util

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_SUCCESS
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.STATUS_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.STATUS_OVERDUE
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.STATUS_SUCCESS

class Util {

    private val logger = loggerFor(javaClass)
    fun checkStatusOrder(orderStatus:OrderStatus?,errorCodeIsPaidFor:Int,errorCodeIsNotAvailable:Int,traceId:String){
        if (orderStatus != null) {
            if (orderStatus.stateId == STATUS_SUCCESS) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_SUCCESS + traceId)
                throw BusinessException(errorCodeIsPaidFor, traceId)
            }
            if (orderStatus.stateId == STATUS_OVERDUE || orderStatus.stateId == STATUS_MARKEDDEL) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL + traceId)
                throw BusinessException(errorCodeIsNotAvailable, traceId)
            }
        }
    }
}