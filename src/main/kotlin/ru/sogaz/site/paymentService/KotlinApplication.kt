package ru.sogaz.site.paymentService

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import ru.sogaz.site.paymentService.properties.AppInfoProperties

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppInfoProperties::class)
@ComponentScan(basePackages = ["ru.sogaz.site.paymentService"])
@EnableFeignClients(basePackages = ["ru.sogaz.site.paymentService.clients"])
@ConfigurationPropertiesScan("ru.sogaz.site.paymentService.properties")
class KotlinApplication

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

fun <T> loggerFor(clazz: Class<T>): Logger = LoggerFactory.getLogger(clazz)

inline fun <reified T> T?.orThrow(block: () -> Exception): T = this ?: throw block()
