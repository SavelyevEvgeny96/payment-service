package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.validation.*

@Configuration
open class ValidatorConfig {
    @Bean
    open fun emailValidator(): EmailValidator {
        val emailRegex = Regex("^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        return EmailValidator(emailRegex)
    }
    @Bean
    fun isValidCorrectInput(): PolicyholderValidator{
        val regex = Regex("^[а-яА-ЯёЁ\\s-]+$")
        val regexDoc = Regex("^[0-9\\s]+$")
        return PolicyholderValidator(regex,regexDoc)
    }
    @Bean
    open fun externalSystemCodeValidator(): ExternalSystemCodeValidator {
        val codeRegex = Regex("^(ADI|FOP|LK|1C)$")
        return ExternalSystemCodeValidator(codeRegex)
    }

    @Bean
    open fun phoneValidator(): PhoneValidator {
        val codeRegex = Regex("^\\+7\\d{10}$")
        return PhoneValidator(codeRegex)
    }

    @Bean
    open fun paymentEndDateFormatValidator(): PaymentEndDateValidatorFormat {
        val codeRegex = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+03:00\$")
        return PaymentEndDateValidatorFormat(codeRegex)
    }
}
