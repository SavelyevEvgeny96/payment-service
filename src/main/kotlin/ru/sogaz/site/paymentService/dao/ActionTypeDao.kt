package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.ActionType

interface ActionTypeDao {
    fun getActionType(
        traceId: String,
        actionType: String,
    ): ActionType
}
