package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.siter.models.resonses.Response

interface GazpromService {
    fun getGPBToken(
        traceId: String,
        order: Order,
        subOrder: SubOrder,
    ): String

    fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>>

    fun initiateGPBSBPPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>>
}
