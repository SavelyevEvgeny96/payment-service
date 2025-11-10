package ru.sogaz.site.paymentService.config

import feign.Client
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.loggingStarter.interceptor.OkHttpLoggingInterceptor
import ru.sogaz.site.loggingStarter.properties.LoggingProperties
import java.util.concurrent.TimeUnit

@Configuration
class OkHttpClientConfig {
    @Bean
    fun okHttpLoggingInterceptor(loggingProperties: LoggingProperties) = OkHttpLoggingInterceptor(loggingProperties)

    @Bean
    fun okHttpClient(okHttpLoggingInterceptor: OkHttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(okHttpLoggingInterceptor)
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build()

    @Bean
    fun feignOkHttpClient(okHttpClient: OkHttpClient): Client = feign.okhttp.OkHttpClient(okHttpClient)
}
