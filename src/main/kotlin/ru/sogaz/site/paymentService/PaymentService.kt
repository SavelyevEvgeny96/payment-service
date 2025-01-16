package ru.sogaz.site.paymentService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PaymentService

fun main(args: Array<String>) {
    runApplication<PaymentService>(*args)
}
