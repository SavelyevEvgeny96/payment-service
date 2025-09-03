package ru.sogaz.site.paymentService.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.Security
import java.security.Signature
import java.util.*

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
        return try {

            val decodedQueryString = java.net.URLDecoder.decode(request.signature, StandardCharsets.UTF_8)

            val decodedSignature = Base64.getDecoder().decode(decodedQueryString)

            verifySignatureCert(decodedSignature, queryString.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            false
        }
    }

    private fun verifySignatureCert(
        signature: ByteArray,
        hash: ByteArray
    ): Boolean =
        try {
            synchronized(preconfiguredSignature) {
                preconfiguredSignature.apply {
                    update(hash)
                    verify(signature)
                }
            }
            true
        } catch (e: Exception) {
            logger.info(VEREFIELD_FAIL, e)
            false
        }
}
