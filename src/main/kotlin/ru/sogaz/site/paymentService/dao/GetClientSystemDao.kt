package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.ClientSystem

interface GetClientSystemDao {
    fun getClientSystem(
        traceId: String,
        externalSystemCode: String,
    ): ClientSystem
}
