package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.ClientSystem

interface AuthorizationService {
    fun checkPermissionByClientId(
        authorizationHeader: String?,
        errorCode: Int? = null,
    ): ClientSystem
}
