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
        return try {
            val hashBytes =
                MessageDigest.getInstance("SHA-256").digest(queryString.toByteArray(StandardCharsets.UTF_8))

            val decodedQueryString = java.net.URLDecoder.decode(request.signature, StandardCharsets.UTF_8)

            val decodedSignature = Base64.getDecoder().decode(decodedQueryString)

            if (verifySignatureCert(decodedSignature, hashBytes)) {
                decodedSignature.containsSubArray(hashBytes)
            } else {
                false
            }
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

    fun ByteArray.containsSubArray(subArray: ByteArray): Boolean {
        if (subArray.isEmpty()) return true
        if (size < subArray.size) return false

        val lastIndex = size - subArray.size + 1
        for (i in 0 until lastIndex) {
            var j = 0
            while (j < subArray.size && this[i + j] == subArray[j]) {
                j++
            }
            if (j == subArray.size) return true
        }
        return false
    }
}
