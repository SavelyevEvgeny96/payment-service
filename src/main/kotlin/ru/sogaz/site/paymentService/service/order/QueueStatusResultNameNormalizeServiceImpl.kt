package ru.sogaz.site.paymentService.service.order

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.service.QueueStatusResultNameNormalizeService

@Component
class QueueStatusResultNameNormalizeServiceImpl(
    private val regex: Regex,
) : QueueStatusResultNameNormalizeService {
    companion object {
        const val PAYMENT_STATUS_PATTERN = "payment.status.%s.created"
        const val ORDER_STATUS_PATTERN = "order.status.reg.%s.created"
    }

    override fun buildQueueStatusResultName(
        pattern: String,
        clientId: String?,
    ): String? {
        if (clientId.isNullOrBlank()) {
            return null
        }

        val normalizedString = clientId.replace(regex, ".")

        return String.format(pattern, normalizedString)
    }
}
