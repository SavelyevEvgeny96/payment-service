package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.entity.SubOrder

interface GeneratorService {
    fun generateDescription(sabOrderList: List<SubOrder>): String

    fun generateUniquePaymentId(): String
    fun getDescriptionAndPremiumAmount(
        premiumAmount: String?,
        listSubOrder: List<SubOrder>
    ): DataDescriptionAndPremiumAmount
}
