package ru.sogaz.site.paymentService.service.order

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_ORDER_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_SUB_ORDER_CROSS_SELL
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.mapper.SubOrderMapper
import ru.sogaz.site.paymentService.service.SubOrderService

class SubOrderServiceImpl(
    private val subOrderDao: SubOrderDao,
    private val subOrderMapper: SubOrderMapper,
) : SubOrderService {
    companion object {
        private const val EMPTY_LIST_SIZE = 0
        private const val NON_CROSS_SELL_SUB_ORDER_LIST_SIZE = 1
    }

    override fun updateSubOrder(updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest): SubOrder {
        val subOrders = subOrderDao.findAllByOrderId(updatePaymentInvoiceRequest.orderId)

        val existedSubOrder = when (subOrders.size) {
            EMPTY_LIST_SIZE -> throw BusinessException(CODE_ERROR_UPDATED_ORDER_NOT_FOUND, getTraceId())
            NON_CROSS_SELL_SUB_ORDER_LIST_SIZE -> subOrders.first()
            else -> throw BusinessException(CODE_ERROR_UPDATED_SUB_ORDER_CROSS_SELL, getTraceId())
        }
        val updatedSubOrder = subOrderMapper.updateSubOrder(updatePaymentInvoiceRequest, existedSubOrder)
        return subOrderDao.save(updatedSubOrder)
    }
}
