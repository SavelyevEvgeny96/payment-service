// WebConfigRestTemplate.kt
package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class WebConfigRestTemplate {
    /**
     * Обычный RestTemplate (без сертификатов)
     */
    @Bean
    fun defaultRestTemplate(): RestTemplate = RestTemplate()
}
