package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
open class JacksonConfig {
    @Bean
    open fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        objectMapper.registerModule(JavaTimeModule())
        return objectMapper
    }

    @Bean
    open fun jsonMessageConverter(): MappingJackson2HttpMessageConverter = MappingJackson2HttpMessageConverter(objectMapper())
}
