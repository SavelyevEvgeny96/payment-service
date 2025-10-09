package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.ClientSystem

interface ClientSystemDao {
    fun getClientSystem(
        traceId: String,
        externalSystemCode: String,
    ): ClientSystem

    fun findBySystemCode(externalSystemCode: String?): ClientSystem?
}
