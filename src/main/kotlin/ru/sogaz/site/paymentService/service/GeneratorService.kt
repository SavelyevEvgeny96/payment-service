package ru.sogaz.site.paymentService.service

import org.springframework.http.HttpHeaders
import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.entity.SubOrder
import java.time.Duration

interface GeneratorService {
    fun generateDescription(sabOrderList: List<SubOrder>?): String

    fun generateUniquePaymentId(): String

    fun getDescriptionAndPremiumAmount(
        premiumAmount: String?,
        listSubOrder: List<SubOrder>?,
    ): DataDescriptionAndPremiumAmount

    fun nowPlusFormatted(
        days: Long = 0,
        minutes: Long = 0,
    ): String

    fun formatDuration(duration: Duration): String

    fun jsonHeaders(): HttpHeaders
}
