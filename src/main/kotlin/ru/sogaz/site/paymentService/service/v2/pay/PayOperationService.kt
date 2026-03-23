package ru.sogaz.site.paymentService.service.v2.pay

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentQrContent
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface PayOperationService {
    fun cardPayOperation(payOperationRequest: CardPayOperationRequest): BankPaymentPageData

    fun sbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentPageData

    fun recurrentOperation(recurrentOperationRequest: CardRecurrentOperationRequest): BankOperationDetails

    fun qrImageSbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentQrContent
}
