package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment

interface BankIntegrationService {
    fun registerPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams? = null,
        recurrent: Boolean
    ): Payment

    fun getQRCodeImageData(payment: Payment): GPBQRImageResponse

    fun requestPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails
}
