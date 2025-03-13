package ru.sogaz.site.paymentService.dao

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface ConfigDataDao {
    fun getBankPriority(traceId: String): String

    fun getCodeLength(traceId: String): Int

    fun generateUniquePaymentCode(traceId: String): String

    fun generateUniquePaymentId(): String = UUID.randomUUID().toString()

    fun getBank(
        bankId: String?,
        traceId: String,
    ): Bank?

    fun getGPBToken(traceId: String,order: Order): String

    fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        premiumAmount: String?,
        order: Order
    ): ResponseEntity<Response<DataPay>>
}
