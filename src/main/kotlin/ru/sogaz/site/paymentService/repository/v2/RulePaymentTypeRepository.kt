package ru.sogaz.site.paymentService.repository.v2

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentService.model.v2.entity.RulePaymentType
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import java.util.UUID

interface RulePaymentTypeRepository : JpaRepository<RulePaymentType, UUID> {
    fun findByBankAndPaymentTypeAndOperationType(
        bank: OperationBank,
        paymentType: PaymentType,
        operationType: OperationType,
    ): RulePaymentType?
}
