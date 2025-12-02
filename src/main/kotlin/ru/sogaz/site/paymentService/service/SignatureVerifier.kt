package ru.sogaz.site.paymentService.service

import jakarta.servlet.http.HttpServletRequest
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest

interface SignatureVerifier {
    fun verifySignature(
        requestDto: GpbCallbackRequest,
        httpServletRequest: HttpServletRequest,
    ): Boolean
}
