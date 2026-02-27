package ru.sogaz.site.paymentService.service.v2.status

import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbCardCallback
import java.util.UUID

interface OperationCallbackService {
    fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback)

    fun updateByGpbSbpCallback(
        orderId: UUID,
        paymentBankId: String,
    )
}
