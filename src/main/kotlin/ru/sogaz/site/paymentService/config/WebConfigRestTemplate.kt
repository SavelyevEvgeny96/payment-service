package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class WebConfigRestTemplate {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
