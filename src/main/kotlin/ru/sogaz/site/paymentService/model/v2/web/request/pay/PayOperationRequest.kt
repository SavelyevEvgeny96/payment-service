package ru.sogaz.site.paymentService.model.v2.web.request.pay

import ru.sogaz.site.paymentService.model.v2.enums.payment.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.payment.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.PayParams
import java.math.BigDecimal

sealed class PayOperationRequest : OperationRequest() {
    override val operationType: OperationType
        get() = OperationType.PAY
    abstract val paymentType: PaymentType
    abstract val description: String
    abstract val amount: BigDecimal
    abstract val payItems: Map<String, String>
    abstract val params: PayParams
}
