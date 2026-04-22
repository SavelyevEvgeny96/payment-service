package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.callback.SignatureVerifierImpl
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.util.Base64
import java.util.regex.Pattern

@Configuration
@Profile(value = ["stage", "prod"])
class SignatureVerifierConfig {
    companion object {
        const val CONST_INSTANCE = "SHA1withRSA"
        const val CONST_X_509 = "X.509"
        const val KEY_ERROR = "Ошибка инициализации публичного ключа"
    }

    @Bean
    fun preconfiguredSignature(gpbConfigProperties: GpbConfigProperties): Signature =
        Signature.getInstance(CONST_INSTANCE).apply {
            initVerify(loadPublicKey(gpbConfigProperties))
        }

    @Bean
    fun signatureVerifier(gpbConfigProperties: GpbConfigProperties): SignatureVerifier {
        val pattern = Pattern.compile("%[0-9a-fA-F]{2}")
        return SignatureVerifierImpl(preconfiguredSignature(gpbConfigProperties), pattern, gpbConfigProperties)
    }

    private fun loadPublicKey(gpbConfigProperties: GpbConfigProperties): PublicKey =
        try {
            val keyBytes =
                gpbConfigProperties.certs
                    .replace("\"", "")
                    .replace("\\n", "\n")
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "")
                    .trim()
                    .let { Base64.getDecoder().decode(it) }

            CertificateFactory
                .getInstance(CONST_X_509)
                .generateCertificate(ByteArrayInputStream(keyBytes))
                .publicKey
        } catch (e: Exception) {
            throw IllegalStateException(KEY_ERROR, e)
        }
}
