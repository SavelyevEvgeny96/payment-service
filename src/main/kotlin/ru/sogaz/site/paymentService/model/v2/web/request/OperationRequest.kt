package ru.sogaz.site.paymentService.model.v2.web.request

import ru.sogaz.site.paymentService.model.v2.enums.payment.OperationType
import java.util.UUID

abstract class OperationRequest {
    abstract val orderId: UUID
    abstract val operationType: OperationType
}
