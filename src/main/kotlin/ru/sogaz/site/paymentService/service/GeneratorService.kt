package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.SubOrder

interface GeneratorService {
    fun generateUniquePaymentCode(traceId: String): String

    fun generateDescription(sabOrderList: List<SubOrder>): String

    fun generateUniquePaymentId(): String
}
