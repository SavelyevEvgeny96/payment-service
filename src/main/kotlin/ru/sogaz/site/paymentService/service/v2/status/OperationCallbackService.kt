package ru.sogaz.site.paymentService.service.v2.status

import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback

interface OperationCallbackService {
    fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback)

    fun updateByQrId(qrId: String)
}
