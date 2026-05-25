package ru.sogaz.site.paymentService.service.v2.rules

import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank

/**
 * Сервис для проверки доступности операции по типу оплаты и банку.
 */
interface RulePaymentTypeService {
    /**
     * Определяет доступность операции.
     *
     * При наличии банка используется точное правило, иначе правило с [OperationBank.ALL].
     */
    fun isOperationAvailable(
        operationType: OperationType,
        paymentType: PaymentType,
        bank: OperationBank? = null,
    ): Boolean
}
