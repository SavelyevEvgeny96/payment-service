package ru.sogaz.site.paymentService.service.callback

import jakarta.servlet.http.HttpServletRequest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.nio.charset.StandardCharsets
import java.security.Security
import java.security.Signature
import java.util.Base64
import java.util.regex.Pattern

class SignatureVerifierImpl(
    private val preconfiguredSignature: Signature,
    private val pattern: Pattern,
    private val gpbConfigProperties: GpbConfigProperties,
) : SignatureVerifier {
    private val logger = loggerFor(javaClass)

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    companion object {
        const val VEREFIELD_FAIL = "Ошибка верификации подписи"
        const val SIGNATURE_PARAMETER = "&signature"
    }

    override fun verifySignature(
        requestDto: GpbCallbackRequest,
        httpServletRequest: HttpServletRequest,
    ): Boolean =
        try {
            val signature = requestDto.signature
            val decodedQueryString =
                if (isEncoded(signature)) {
                    java.net.URLDecoder.decode(signature, StandardCharsets.UTF_8)
                } else {
                    signature
                }

            val decodedSignature = Base64.getDecoder().decode(decodedQueryString)

            verifySignatureCert(decodedSignature, httpServletRequest)
        } catch (e: Exception) {
            logger.error(VEREFIELD_FAIL)
            false
        }

    private fun isEncoded(signature: String): Boolean = pattern.matcher(signature).find()

    private fun verifySignatureCert(
        signature: ByteArray,
        httpServletRequest: HttpServletRequest,
    ): Boolean =
        try {
            synchronized(preconfiguredSignature) {
                updateRequestDto(preconfiguredSignature, httpServletRequest)
                preconfiguredSignature.verify(signature)
            }
        } catch (e: Exception) {
            logger.debug(VEREFIELD_FAIL, e)
            false
        }

    private fun updateRequestDto(
        signature: Signature,
        httpServletRequest: HttpServletRequest,
    ) {
        val urlBytes = gpbConfigProperties.callbackUrl.toByteArray(Charsets.UTF_8)
        signature.update(urlBytes)
        val queryStr = "?" + httpServletRequest.queryString
        val paramStr = queryStr.substringBefore(SIGNATURE_PARAMETER)
        val paramBytes = paramStr.toByteArray(Charsets.UTF_8)
        signature.update(paramBytes)
    }
}
