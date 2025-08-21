package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.siter.models.resonses.Response

interface GazpromService {
    fun getGPBToken(
        order: Order,
        subOrder: SubOrder,
    ): String

    fun initiateGPBPayment(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
        tokenGpb: String,
        paymentId: Long?,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>>

    fun initiateGPBSBPPayment(
        paymentId: Long?,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>>
}
