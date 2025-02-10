package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.validation.PaymentRequestValidator

@Configuration
open class PaymentRequestValidatorConfig {
    @Bean
    open fun paymentRequestValidator(): PaymentRequestValidator = PaymentRequestValidator()
}
