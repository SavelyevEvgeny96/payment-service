package ru.sogaz.site.paymentService.properties.gpb

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.gpb.card.auth.retryer")
data class AuthClientRetryerProperties(
    val maxRetries: Int,
    val minTimeoutMs: Long,
    val maxTimeoutMs: Long,
)