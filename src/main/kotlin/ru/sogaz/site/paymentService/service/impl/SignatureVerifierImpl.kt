package ru.sogaz.site.paymentService.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.Security
import java.security.Signature
import java.util.Base64

class SignatureVerifierImpl(
    private val preconfiguredSignature: Signature,
) : SignatureVerifier {
    private val logger = loggerFor(javaClass)

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    companion object {
        const val VEREFIELD_FAIL = "Ошибка верификации подписи"
        const val SIGNATURE_NULL = "Строка &signature= пуста"
    }

    override fun verifySignature(
        request: GpbCallbackRequest,
        queryString: String,
    ): Boolean {
        try {
            val hashBytes =
                MessageDigest.getInstance("SHA-256").digest(queryString.toByteArray(StandardCharsets.UTF_8))
            val decodedSignature = Base64.getDecoder().decode(request.signature)


            return if (verifySignatureCert(decodedSignature)) {
                hashBytes.contentEquals(
                    MessageDigest.getInstance("SHA-256").digest(decodedSignature)
                )
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            return false
        }
    }

    private fun verifySignatureCert(
        signature: ByteArray,
    ): Boolean =
        try {
            synchronized(preconfiguredSignature) {
                preconfiguredSignature.apply {
                    update(signature)
                    verify(signature)
                }
            }
            true
        } catch (e: Exception) {
            logger.info(VEREFIELD_FAIL, e)
            false
        }
}
