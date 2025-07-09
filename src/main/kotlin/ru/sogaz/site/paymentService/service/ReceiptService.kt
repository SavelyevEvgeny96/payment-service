package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.Order

interface ReceiptService {
    fun generateReceipt(order: Order)
}