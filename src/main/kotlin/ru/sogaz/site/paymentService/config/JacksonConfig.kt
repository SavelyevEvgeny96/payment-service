package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
open class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        val kotlinModule =
            KotlinModule
                .Builder()
                .build()

        return ObjectMapper()
            .registerModule(kotlinModule)
    }

    @Bean
    open fun jsonMessageConverter(): MappingJackson2HttpMessageConverter = MappingJackson2HttpMessageConverter(objectMapper())
}
