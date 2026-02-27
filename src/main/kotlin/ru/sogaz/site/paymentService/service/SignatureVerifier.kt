package ru.sogaz.site.paymentService.service

import jakarta.servlet.http.HttpServletRequest
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbCardCallback

interface SignatureVerifier {
    fun verifySignature(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): Boolean

    fun verifySignature(
        gpbCallback: GpbCardCallback,
        httpServletRequest: HttpServletRequest,
    ): Boolean
}
