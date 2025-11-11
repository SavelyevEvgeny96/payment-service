package ru.sogaz.site.paymentService.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
)
@OpenAPIDefinition(
    security = [SecurityRequirement(name = "bearerAuth")],
)
class OpenApiConfiguration {
    @Bean
    fun apiV1(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("main")
            .pathsToMatch("/payment/**")
            .displayName("API v1")
            .build()

    @Bean
    @Profile("test", "local")
    fun adminApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("test")
            .pathsToMatch("/admin/payment/**")
            .displayName("API для администрирования")
            .build()
}
