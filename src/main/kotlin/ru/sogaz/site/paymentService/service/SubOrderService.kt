package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.SubOrder

interface SubOrderService {
    fun updateSubOrder(updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest): SubOrder
}
