package ru.sogaz.site.paymentService.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
open class ApiConfig {
    @Value("\${api.payment-url}")
    lateinit var paymentUrl: String
}

