package ru.sogaz.site.paymentService.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

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
    ): Boolean {
        try {

            val hashBytes = MessageDigest.getInstance("SHA-256").digest(queryString.toByteArray())
            val hashBase64 = Base64.getEncoder().encodeToString(hashBytes)

            val decodedSignature = Base64.getDecoder().decode(request.signature)
            val privateKeyObj = getCertificateFromString(gpbConfigProperties.gpb)
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKeyObj)
            val decryptedData = String(cipher.doFinal(decodedSignature))

            return decryptedData == hashBase64
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            return false
        }
    }


    private fun getCertificateFromString(certificateStr: String): X509Certificate {
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
            return cf.generateCertificate(inputStream) as X509Certificate
        } catch (e: Exception) {
            throw e
        }
    }
}
