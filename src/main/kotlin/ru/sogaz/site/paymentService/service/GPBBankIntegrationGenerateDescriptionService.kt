package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DescriptionInfo
import ru.sogaz.site.paymentService.entity.Order

interface GPBBankIntegrationGenerateDescriptionService {
    fun makeDescription(order: Order): DescriptionInfo
}
