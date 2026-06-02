package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.Payment

interface ReceiptService {
    fun generateReceipt(payment: Payment)
}
