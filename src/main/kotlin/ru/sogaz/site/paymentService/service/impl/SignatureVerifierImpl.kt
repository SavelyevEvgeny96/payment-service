package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.security.Signature
import java.util.Base64

class SignatureVerifierImpl(
    private val preconfiguredSignature: Signature,
) : SignatureVerifier {
    private val logger = loggerFor(javaClass)

    companion object {
        const val VEREFIELD_FAIL = "Ошибка верификации подписи"
    }

    override fun verifySignature(signatureBase64: String): Boolean =
        try {
            val signatureBytes = Base64.getDecoder().decode(signatureBase64)
            synchronized(preconfiguredSignature) {
                preconfiguredSignature.apply {
                    update(signatureBytes)
                    verify(signatureBytes)
                }
            }
            true
        } catch (e: Exception) {
            logger.info(VEREFIELD_FAIL, e)
            false
        }
}
