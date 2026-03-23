package ru.sogaz.site.paymentService.config.feign

import feign.Retryer
import org.springframework.context.annotation.Bean
import ru.sogaz.site.paymentService.properties.gpb.AuthClientRetryerProperties

class GpbAuthRetryerConfig(
    private val retryerProperties: AuthClientRetryerProperties,
) {
    @Bean
    fun authRetryer(): Retryer =
        Retryer.Default(
            retryerProperties.minTimeoutMs,
            retryerProperties.maxTimeoutMs,
            retryerProperties.maxRetries,
        )
}
