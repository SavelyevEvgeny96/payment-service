package ru.sogaz.site.paymentService.service.v2.status

import ru.sogaz.site.paymentService.dto.data.SbpGpbStateCallbackRequest
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbGidCallbackRequest

interface OperationCallbackService {
    fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback)

    fun updateByGpbGidCallback(request: GpbGidCallbackRequest)

    fun processSbpReversalCallback(paymentBankId: String)

    fun updateByQrId(request: SbpGpbStateCallbackRequest)
}
