package ru.sogaz.site.paymentService.service.v2.status

import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import java.util.UUID

interface OperationCallbackService {
    fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback)

    fun updateByOrderIdAndPaymentBankId(
        orderId: UUID,
        paymentBankId: String,
    )

    fun processSbpReversalCallback(paymentBankId: String)
}
