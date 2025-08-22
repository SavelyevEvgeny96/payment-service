package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.AkbBankIntegrationService
import ru.sogaz.siter.models.resonses.Response

class AkbBankIntegrationServiceImpl() : AkbBankIntegrationService {

    companion object {
        const val MESSAGE_INFO_START_AKB_PAYMENT =
            ">>> СТАРТ метода оплата картой Банк Россия для платежа с payment_id: "
        const val MESSAGE_INFO_END_AKB_PAYMENT = "<<< КОНЕЦ метода оплата картой Банк Россия для платежа с payment_id: "
    }

    private val logger = loggerFor(javaClass)
    override fun initiateAKBPayment(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
        paymentId: Long?,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder
    ): ResponseEntity<Response<DataPay>> {
        logger.info("$MESSAGE_INFO_START_AKB_PAYMENT $paymentId")

    }
}