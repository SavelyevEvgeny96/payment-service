package ru.sogaz.site.paymentService.config

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class OkHttpClientConfig {

    @Bean
    fun okHttpClient(loggingInterceptor: Interceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build()

    @Bean
    fun feignOkHttpClient(okHttpClient: OkHttpClient): feign.okhttp.OkHttpClient = feign.okhttp.OkHttpClient(okHttpClient)
}