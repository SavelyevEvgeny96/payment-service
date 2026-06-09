package ru.sogaz.site.paymentService.service.v2.bank.selection

import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest

interface OperationBankSelectionService {
    fun selectBank(payOperationRequest: PayOperationRequest): OperationBank
}
