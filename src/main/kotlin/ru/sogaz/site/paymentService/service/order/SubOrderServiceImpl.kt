package ru.sogaz.site.paymentService.service.order

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_ORDER_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_SUB_ORDER_CROSS_SELL
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.SubOrderMapper
import ru.sogaz.site.paymentService.service.SubOrderService

@Service
class SubOrderServiceImpl(
    private val subOrderDao: SubOrderDao,
    private val subOrderMapper: SubOrderMapper,
) : SubOrderService {
    companion object {
        private const val EMPTY_LIST_SIZE = 0
        private const val NON_CROSS_SELL_SUB_ORDER_LIST_SIZE = 1
        const val LOG_UPDATE_SUB_ORDER_PAYMENT_INVOICE = "Начало обновления информации о заказе с orderId = "
        const val LOG_SUCCESS_UPDATE_SUB_ORDER_PAYMENT_INVOICE = "Успешное обновление информации о заказе с orderId = "
    }

    private val logger = loggerFor(javaClass)

    override fun updateSubOrder(updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest): SubOrder {
        logger.info(LOG_UPDATE_SUB_ORDER_PAYMENT_INVOICE + updatePaymentInvoiceRequest.orderId)
        val subOrders = subOrderDao.findAllByOrderId(updatePaymentInvoiceRequest.orderId)

        val existedSubOrder =
            when (subOrders.size) {
                EMPTY_LIST_SIZE -> throw BusinessException(CODE_ERROR_UPDATED_ORDER_NOT_FOUND, getTraceId())
                NON_CROSS_SELL_SUB_ORDER_LIST_SIZE -> subOrders.first()
                else -> throw BusinessException(CODE_ERROR_UPDATED_SUB_ORDER_CROSS_SELL, getTraceId())
            }
        val updatedSubOrder = subOrderMapper.updateSubOrder(updatePaymentInvoiceRequest, existedSubOrder)
        logger.info(LOG_SUCCESS_UPDATE_SUB_ORDER_PAYMENT_INVOICE + updatePaymentInvoiceRequest.orderId)
        return subOrderDao.save(updatedSubOrder)
    }
}
