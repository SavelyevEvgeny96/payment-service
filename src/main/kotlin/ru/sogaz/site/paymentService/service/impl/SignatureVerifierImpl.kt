package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.security.Signature
import java.util.Base64

class SignatureVerifierImpl(
    private val gpbConfigProperties: GpbConfigProperties,
) : SignatureVerifier {
    private val logger = loggerFor(javaClass)

    override fun verifySignature(signatureBase64: String): Boolean =
        try {
            val signatureBytes = Base64.getDecoder().decode(signatureBase64)
            val signature =
                Signature.getInstance("SHA1withRSA").apply {
                    initVerify(loadPublicKey())
                }
            signature.verify(signatureBytes)
            true
        } catch (e: Exception) {
            logger.info("Signature verification failed", e)
            false
        }

    private fun loadPublicKey(): PublicKey =
        try {
            val keyBytes =
                gpbConfigProperties.gpb
                    .lines()
                    .filter { it.isNotBlank() && !it.startsWith("-----") }
                    .joinToString("")
                    .trim()

            val certBytes = Base64.getDecoder().decode(keyBytes)

            val certFactory =
                java.security.cert.CertificateFactory
                    .getInstance("X.509")
            val cert = certFactory.generateCertificate(ByteArrayInputStream(certBytes))

            cert.publicKey
        } catch (e: Exception) {
            logger.info("Ошибка загрузки ключа")
            throw InnerException(RequestInfo.getTraceId(), e.message)
        }
}
