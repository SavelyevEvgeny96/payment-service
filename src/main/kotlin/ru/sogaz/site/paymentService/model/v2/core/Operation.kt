package ru.sogaz.site.paymentService.model.v2.core

import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import java.util.UUID

abstract class Operation {
    abstract val id: UUID
    abstract val operationType: OperationType
}
