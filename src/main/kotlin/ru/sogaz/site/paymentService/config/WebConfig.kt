package ru.sogaz.site.paymentService.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.validation.filter.TokenValidationFilter

@Configuration
class WebConfig : WebMvcConfigurer {
    @Bean
    fun tokenValidationFilter(tokenService: TokenService): FilterRegistrationBean<TokenValidationFilter> {
        val registrationBean = FilterRegistrationBean<TokenValidationFilter>()
        registrationBean.filter = TokenValidationFilter(tokenService)
        registrationBean.addUrlPatterns("/create")
        return registrationBean
    }
}