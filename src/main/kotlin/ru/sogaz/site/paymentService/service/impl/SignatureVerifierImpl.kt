package ru.sogaz.site.paymentService.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

class SignatureVerifierImpl(
    private val gpbConfigProperties: GpbConfigProperties,
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
            val hashBytes = MessageDigest.getInstance("SHA-256").digest(queryString.toByteArray())

            val decodedSignature = Base64.getDecoder().decode(request.signature)

            val publicKey: PublicKey = getCertificateFromString(gpbConfigProperties.gpb)

            val signature = Signature.getInstance("SHA1withRSA")
            signature.initVerify(publicKey)
            signature.update(hashBytes)

            signature.verify(decodedSignature)
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            false
        }

    private fun getCertificateFromString(certificateStr: String): PublicKey {
        val keyBytes =
            certificateStr
                .replace("\"", "")
                .replace("\\n", "\n")
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")
                .trim()
                .let { Base64.getDecoder().decode(it) }

        try {
            val inputStream = ByteArrayInputStream(keyBytes)
            val cf = CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(inputStream) as X509Certificate
            return cert.publicKey
        } catch (e: Exception) {
            throw e
        }
    }
}
