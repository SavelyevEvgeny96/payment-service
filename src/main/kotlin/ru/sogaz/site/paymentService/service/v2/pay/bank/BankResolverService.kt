package ru.sogaz.site.paymentService.service.v2.pay.bank

import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest

interface BankResolverService {
    fun resolveBank(cardPayOperationRequest: CardPayOperationRequest): BankEnum
}
