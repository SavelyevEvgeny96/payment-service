package ru.sogaz.site.paymentService.dto

import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.SubOrder

data class PaymentContext(
    val order: Order,
    val subOrder: SubOrder,
    val premiumAmount: String?,
    val orderStatus: OrderStatus?,
    val configBankPriorityCheck: String,
    val checkBank: Bank?
)