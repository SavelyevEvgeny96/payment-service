package ru.sogaz.site.paymentService.config

import feign.Retryer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.gpb.AuthClientRetryerProperties

@Configuration
class OkHttpClientConfig(
    private val retryerProperties: AuthClientRetryerProperties,
) {
    @Bean
    fun okHttpClient(loggingInterceptor: Interceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @Bean
    fun feignOkHttpClient(okHttpClient: OkHttpClient): feign.okhttp.OkHttpClient = feign.okhttp.OkHttpClient(okHttpClient)

    @Bean
    fun retryer(): Retryer =
        Retryer.Default(
            retryerProperties.minTimeoutMs,
            retryerProperties.maxTimeoutMs,
            retryerProperties.maxRetries,
        )
}
