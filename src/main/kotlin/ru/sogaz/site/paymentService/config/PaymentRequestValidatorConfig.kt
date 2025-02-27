package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.validation.*

@Configuration
open class PaymentRequestValidatorConfig {
    @Bean
    open fun paymentRequestValidator(
        phoneValidator: PhoneValidator,
        paymentEndDateValidatorFormat: PaymentEndDateValidatorFormat,
        emailValidator: EmailValidator,
        externalSystemCodeValidator: ExternalSystemCodeValidator
    ): PaymentRequestValidator = PaymentRequestValidator(
        emailValidator = emailValidator,
        externalSystemCodeValidator = externalSystemCodeValidator,
        phoneValidator = phoneValidator,
        paymentEndDateValidatorFormat = paymentEndDateValidatorFormat
    )
}
