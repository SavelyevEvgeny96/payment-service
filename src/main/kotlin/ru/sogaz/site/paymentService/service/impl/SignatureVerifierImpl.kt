package ru.sogaz.site.paymentService.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

class SignatureVerifierImpl(
    private val gpbConfigProperties: GpbConfigProperties,
    private val preconfiguredSignature: Signature
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
    ): Boolean =
        try {
            val signature = preconfiguredSignature
            val hashBytes =
                MessageDigest.getInstance("SHA-256").digest(queryString.toByteArray(StandardCharsets.UTF_8))

            signature.update(hashBytes)

            val isVerified = signature.verify(Base64.getDecoder().decode(request.signature))

            isVerified
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            false
        }
}
