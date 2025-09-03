package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest

interface SignatureVerifier {
    fun verifySignature(request: GpbCallbackRequest): Boolean
}
