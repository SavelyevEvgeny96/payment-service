package ru.sogaz.site.paymentService.properties.gpb

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.gpb.gid.retry.status")
data class GpbGidStatusRetryProperties(
    val maxRetries: Int,
    val timeoutMs: Long,
)
