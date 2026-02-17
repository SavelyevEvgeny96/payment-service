package ru.sogaz.site.paymentService.service

import jakarta.servlet.http.HttpServletRequest
import ru.sogaz.site.paymentService.dto.request.GpbCallback

interface SignatureVerifier {
    fun verifySignature(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): Boolean
}
