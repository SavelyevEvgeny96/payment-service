package ru.sogaz.site.paymentService.service

interface QueueStatusResultNameNormalizeService {
    fun buildQueueStatusResultName(
        pattern: String,
        clientId: String?,
    ): String?
}
