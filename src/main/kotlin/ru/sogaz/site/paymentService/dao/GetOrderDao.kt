package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order

interface GetOrderDao {
    fun getOrderByCode(code:String,traceId : String): Order
}