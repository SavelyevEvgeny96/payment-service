package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.siter.models.resonses.Response

interface AkbBankIntegrationService {
    fun initiateAKBPayment(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
        paymentId: Long?,
        premiumAmount: String,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>>
}
