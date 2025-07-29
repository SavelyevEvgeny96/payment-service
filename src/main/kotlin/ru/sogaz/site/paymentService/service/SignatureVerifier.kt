package ru.sogaz.site.paymentService.service

interface SignatureVerifier {
    fun verifySignature(
        data: String,
        signatureBase64: String,
    ): Boolean
}
