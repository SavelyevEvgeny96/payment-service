package ru.sogaz.site.paymentService.service.callback

import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.nio.charset.StandardCharsets
import java.security.Security
import java.security.Signature
import java.util.Base64
import java.util.regex.Pattern

class SignatureVerifierImpl(
    private val preconfiguredSignature: Signature,
    private val pattern: Pattern,
) : SignatureVerifier {
    private val logger = loggerFor(javaClass)

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    companion object {
        const val VEREFIELD_FAIL = "Ошибка верификации подписи"
        const val SIGNATURE_NULL = "Строка &signature= пуста"
    }

    override fun verifySignature(request: GpbCallbackRequest): Boolean =
        try {
            val signature = request.signature
            val decodedQueryString =
                if (isEncoded(signature)) {
                    java.net.URLDecoder.decode(signature, StandardCharsets.UTF_8)
                } else {
                    signature
                }

            val decodedSignature = Base64.getDecoder().decode(decodedQueryString)

            verifySignatureCert(decodedSignature)
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            false
        }

    private fun isEncoded(signature: String): Boolean = pattern.matcher(signature).find()

    private fun verifySignatureCert(signature: ByteArray): Boolean =
        try {
            synchronized(preconfiguredSignature) {
                preconfiguredSignature.apply {
                    update(signature)
                    verify(signature)
                }
            }
            true
        } catch (e: Exception) {
            logger.debug(VEREFIELD_FAIL, e)
            false
        }
}
