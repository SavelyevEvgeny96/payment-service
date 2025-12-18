package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.paymentService.service.order.QueueStatusResultNameNormalizeServiceImpl

@Configuration
class ServicesConfig {

    @Bean
    fun queueStatusResultNameNormalizeService(): QueueStatusResultNameNormalizeService {
        val regex = Regex("[^A-Za-zА-Яа-яЁё0-9]")
        return QueueStatusResultNameNormalizeServiceImpl(regex)
    }
}