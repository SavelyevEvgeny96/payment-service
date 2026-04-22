package ru.sogaz.site.paymentService.config.feign

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OkHttpClientConfig {
    @Bean
    fun okHttpClient(loggingInterceptor: Interceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @Bean
    fun feignOkHttpClient(okHttpClient: OkHttpClient): feign.okhttp.OkHttpClient = feign.okhttp.OkHttpClient(okHttpClient)
}
