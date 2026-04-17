package ru.sogaz.site.paymentService.service.callback

import jakarta.servlet.http.HttpServletRequest
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.service.SignatureVerifier

class MockSignatureVerifierImpl : SignatureVerifier {
    override fun verifySignature(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): Boolean = true

    override fun verifySignature(
        gpbCallback: GpbCardCallback,
        httpServletRequest: HttpServletRequest,
    ): Boolean = true
}
