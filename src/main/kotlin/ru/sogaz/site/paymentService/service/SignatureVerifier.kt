package ru.sogaz.site.paymentService.service

interface SignatureVerifier {
    fun verifySignature(signatureBase64: String): Boolean
}
