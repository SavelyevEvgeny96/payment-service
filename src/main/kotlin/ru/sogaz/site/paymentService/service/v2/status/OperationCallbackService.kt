package ru.sogaz.site.paymentService.service.v2.status

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCallbackResponse
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.siter.models.resonses.Response

interface OperationCallbackService {
    fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback)

    fun updateByPaymentBankId(paymentBankId: String)
}
