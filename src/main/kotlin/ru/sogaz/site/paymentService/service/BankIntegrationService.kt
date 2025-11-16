package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import java.util.*

interface BankIntegrationService {
    fun registerPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams? = null,
        orderId: Order?
    ): Payment

    fun getQRCodeImageData(payment: Payment): GPBQRImageResponse

    fun requestPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails
}
