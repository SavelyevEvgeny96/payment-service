package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.AkbCardPaymentRequest
import ru.sogaz.site.paymentService.dto.request.OrderDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.SslClientProperties
import ru.sogaz.site.paymentService.service.AkbBankIntegrationService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.END__METHOD_PAY_BANK_CARD
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.SAVE_OPERATION_HISTORY_START_PAY
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.SUCCESS
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.RUB
import ru.sogaz.siter.models.resonses.Response

class AkbBankIntegrationServiceImpl(
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val apiConfigProperty: ApiConfigProperties,
    private val subOrderDao: SubOrderDao,
    private val generatorService: GeneratorService,
    private val restTemplate: WebConfigRestTemplate,
    private val objectMapper: ObjectMapper,
    private val paymentDao: PaymentDao,
    private val props:SslClientProperties
) : AkbBankIntegrationService {
    companion object {
        const val RU = "ru"
        const val ORDER = "order"
        const val HPP_URL = "hppUrl"
        const val WITH_3DS = "WITH_3DS"
        //нет кода 200 для AKB
        const val STATUS_CODE_SUCCESS_PAY_CARD_AKB_BANK = 200
        const val LOG_SUCCESSFUL_AKB_API = "Успешный запрос к AKB API."
        const val MESSAGE_INFO_START_AKB_PAYMENT =
            ">>> СТАРТ метода оплата картой Банк Россия для платежа с payment_id: "
        const val ACTION_TYPE_START_PAYMENT_AKB = "Отправка запроса для регистрации заказа в АКБ Россия"
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
        val traceId = getTraceId()
        val clientSystem = subOrder.clientSystem
        paymentOperationHistoryDao.saveRecordOperationHistory(
            order,
            clientSystem,
            traceId,
            ACTION_TYPE_START_PAYMENT_AKB
        )
        logger.info("$SAVE_OPERATION_HISTORY_START_PAY $ACTION_TYPE_START_PAYMENT_AKB")
        val url = apiConfigProperty.akbUrl
        val urlTuSuccess = urlToReturn ?: apiConfigProperty.backUrlS
        val listSubOrder = subOrderDao.getAllSubOrderListByOrderId(order, traceId)
        val descAndPremiumAmountData = generatorService.getDescriptionAndPremiumAmount(premiumAmount, listSubOrder)
        val akbCardPaymentRequest = AkbCardPaymentRequest(
            OrderDto(
                typeRid = WITH_3DS,
                amount = descAndPremiumAmountData.premiumAmount?.toInt(),
                currency = RUB,
                hppRedirectUrl = urlTuSuccess,
                ridByMerchant = paymentId.toString(),
                //Уточнить что сюда ложить
                adviceIfaceAddress = urlTuSuccess,
                description = descAndPremiumAmountData.description,
                descriptionHtml = descAndPremiumAmountData.description,
                language = RU
            )
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntity = HttpEntity(akbCardPaymentRequest, headers)
        try {
            logger.info(
                "AKB payment Card request [traceId=$traceId]:  body=\n${objectMapper.writeValueAsString(requestEntity.body)}",
            )
            val responseEntity: ResponseEntity<Map<String, Any>> =
                restTemplate.xpgRestTemplate(props).exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<Map<String, Any>>() {},
                )
            logger.info(LOG_SUCCESSFUL_AKB_API)
            val responseBody = responseEntity.body
            logger.info(
                "AKB payment Card response [traceId=$traceId]: body=$responseBody",
            )
            val paymentPageUrl =
                (responseBody?.get(ORDER) as? Map<*, *>)?.get(HPP_URL) as? String ?: ""
            val result =
                Response(
                    status = SUCCESS,
                    code = STATUS_CODE_SUCCESS_PAY_CARD_AKB_BANK,
                    traceId = traceId,
                    data = DataPay(paymentPageUrl),
                )
            paymentDao.paymentUpdate(paymentId, paymentPageUrl, "")
            logger.info("$END__METHOD_PAY_BANK_CARD $paymentId")
            logger.info(MESSAGE_INFO_END_AKB_PAYMENT)
            return ResponseEntity.ok(result)
        } finally {

        }
    }
}