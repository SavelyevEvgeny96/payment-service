package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.GpbConfigProperties
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.impl.SignatureVerifierImpl

@Configuration
class SignatureVerifierConfig {
    @Bean
    fun signatureVerifier(gpbConfigProperties: GpbConfigProperties): SignatureVerifier = SignatureVerifierImpl(gpbConfigProperties)
}
