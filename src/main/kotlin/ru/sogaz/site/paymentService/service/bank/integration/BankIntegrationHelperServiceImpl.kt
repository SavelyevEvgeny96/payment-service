package ru.sogaz.site.paymentService.service.bank.integration

import ru.sogaz.site.paymentService.entity.Order

abstract class BankIntegrationHelperServiceImpl {
    abstract fun makeDescription(order: Order): String
}
