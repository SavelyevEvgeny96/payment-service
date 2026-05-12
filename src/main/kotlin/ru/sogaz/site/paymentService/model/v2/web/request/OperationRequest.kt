package ru.sogaz.site.paymentService.model.v2.web.request

import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.math.BigDecimal
import java.util.UUID

abstract class OperationRequest {
    abstract val orderId: UUID?
    abstract val amount: BigDecimal
    abstract val operationType: OperationType
    abstract val paymentType: PaymentType
}
