package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.validation.EmailValidator
import ru.sogaz.site.paymentService.validation.ExternalSystemCodeValidator
import ru.sogaz.site.paymentService.validation.PaymentEndDateValidatorFormat
import ru.sogaz.site.paymentService.validation.PaymentRequestValidator
import ru.sogaz.site.paymentService.validation.PhoneValidator
import ru.sogaz.site.paymentService.validation.PolicyholderValidator

@Configuration
open class PaymentRequestValidatorConfig {
    @Bean
    open fun paymentRequestValidator(
        phoneValidator: PhoneValidator,
        paymentEndDateValidatorFormat: PaymentEndDateValidatorFormat,
        emailValidator: EmailValidator,
        externalSystemCodeValidator: ExternalSystemCodeValidator,
        policyholderValidator: PolicyholderValidator,
    ): PaymentRequestValidator =
        PaymentRequestValidator(
            emailValidator = emailValidator,
            externalSystemCodeValidator = externalSystemCodeValidator,
            phoneValidator = phoneValidator,
            paymentEndDateValidatorFormat = paymentEndDateValidatorFormat,
            policyholderValidator = policyholderValidator,
        )
}
