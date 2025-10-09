package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.jwt.starter.service.JwtService
import ru.sogaz.site.paymentService.dao.ClientSystemDao
import ru.sogaz.site.paymentService.validation.PermissionValidator
import ru.sogaz.site.paymentService.validation.constraint.EmailValidator

@Configuration
open class ValidatorConfig {
    companion object {
        const val EMAIL_REGEX_STRING = "^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    }

    @Bean
    open fun emailValidator(): EmailValidator =
        Regex(EMAIL_REGEX_STRING)
            .run(::EmailValidator)

    @Bean
    open fun tokenValidator(
        clientSystemDao: ClientSystemDao,
        jwtService: JwtService,
    ) = PermissionValidator(
        clientSystemDao = clientSystemDao,
        jwtService = jwtService,
    )
}
