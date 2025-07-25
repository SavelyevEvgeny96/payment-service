package ru.sogaz.site.paymentService.service.impl

import org.springframework.beans.factory.annotation.Value
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class SignatureVerifier(
    private val gpbConfigProperties: GpbConfigProperties
) {
    private val logger = loggerFor(javaClass)
    private val publicKey: PublicKey by lazy { loadPublicKey() }

    fun verifySignature(data: String, signatureBase64: String): Boolean {
        return try {
            val signatureBytes = Base64.getDecoder().decode(signatureBase64)
            val signature = Signature.getInstance("SHA1withRSA").apply {
                initVerify(publicKey)
                update(data.toByteArray(Charsets.UTF_8))
            }
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            logger.info("Signature verification failed", e)
            false
        }
    }

    private fun loadPublicKey(): PublicKey {
        val keyBytes = gpbConfigProperties.gpb
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .trim()
            .let { Base64.getDecoder().decode(it) }

        return KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(keyBytes))
    }
}