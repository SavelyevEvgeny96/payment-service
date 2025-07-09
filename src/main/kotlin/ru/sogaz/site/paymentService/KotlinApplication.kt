package ru.sogaz.site.paymentService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import ru.sogaz.site.paymentService.properties.AppInfoProperties

@SpringBootApplication
@ConfigurationPropertiesScan("ru.sogaz.site.paymentService.properties")
@ComponentScan(basePackages = ["ru.sogaz.site.paymentService"])
@EnableConfigurationProperties(AppInfoProperties::class)
open class KotlinApplication

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

fun <T> loggerFor(clazz: Class<T>) =
    ru.sogaz.core.logger.LoggerFactory
        .getApiLogger(clazz)
