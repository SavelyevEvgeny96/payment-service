package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.data.DataPaymentUpdate
import ru.sogaz.site.paymentService.dto.request.AkbCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.dto.request.OrderDto
import ru.sogaz.site.paymentService.dto.request.PreparePushTranRequest
import ru.sogaz.site.paymentService.dto.request.SetSrcTokenRequest
import ru.sogaz.site.paymentService.dto.response.AkbOrderResponse
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.SslClientProperties
import ru.sogaz.site.paymentService.service.AkbBankIntegrationService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.DATA
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.END__METHOD_PAY_BANK_CARD
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.ERROR_GPB_PAYMENT_PROCESSING
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.LOG_SUCCESSFUL_GPB_API_SBP
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.PAYLOAD
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.QRC_ID
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.SAVE_OPERATION_HISTORY_START_PAY
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.SAVE_OPERATION_HISTORY_START_PAY_SBP_ERROR
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.RUB
import ru.sogaz.siter.models.resonses.Response
import java.time.LocalDateTime

class AkbBankIntegrationServiceImpl(
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val apiConfigProperty: ApiConfigProperties,
    private val subOrderDao: SubOrderDao,
    private val generatorService: GeneratorService,
    private val restTemplate: WebConfigRestTemplate,
    private val objectMapper: ObjectMapper,
    private val paymentDao: PaymentDao,
    private val props: SslClientProperties,
) : AkbBankIntegrationService {
    companion object {
        const val RU = "ru"
        const val SET_SRC_TOKEN_SUFFIX = "set-src-token?password="
        const val PUSH_TRAN_SUFFIX = "prepare-push-tran?password="
        const val PAYMENT_ID_IS_REQUIRED = "paymentId is required"
        const val ID = "id"
        const val REDIRECT_URL = "afterPayRedirectUrl"
        const val IPS_RU = "ipsRu"
        const val EMPTY_HPP_URL_RESPONSE = "Пустой HPP URL в ответе Банка Россия"
        const val EMPTY_ORDER_RESPONSE = "Пустой 'order' в ответе Банка Россия"
        const val QRC_PAY = "QRC_PAY"
        const val WITH_3DS = "WITH_3DS"
        const val ERROR_AKB_PAYMENT_PROCESSING_SBP = "Ошибка при отправке запроса для получения платежной ссылки"
        const val ERROR_AKB_PAYMENT_PROCESSING = "Ошибка обработки платежа AKB BANK "
        const val SAVE_OPERATION_HISTORY_START_PAY_ERROR_AKB_BANK =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа  БАНК РОССИЯ "

        const val START_METHOD_PAY_AKB_BANK_SBP =
            ">>> СТАРТ метода оплата по СБП  Банк Россия для платежа с payment_id: "
        const val END__METHOD_PAY_AKB_BANK_SBP =
            "<<< КОНЕЦ  метода оплата по СБП  Банк Россия для платежа с payment_id: "
        const val STATUS_CODE_SUCCESS_PAY_CARD_AKB_BANK = 200
        const val MESSAGE_INFO_START_AKB_PAYMENT =
            ">>> СТАРТ метода оплата картой Банк Россия для платежа с payment_id: "
        const val ACTION_TYPE_START_PAYMENT_AKB = "Отправка запроса для регистрации заказа в АКБ Россия"
        const val MESSAGE_INFO_END_AKB_PAYMENT = "<<< КОНЕЦ метода оплата картой Банк Россия для платежа с payment_id: "
    }

    private val logger = loggerFor(javaClass)

    override fun initiateAKBPayment(
        urlToReturn: String?,
        urlToReturnF: String?,
        paymentId: Long?,
        premiumAmount: String,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>> {
        logger.info("$MESSAGE_INFO_START_AKB_PAYMENT $paymentId")
        val traceId = getTraceId()
        val clientSystem = subOrder.clientSystem
        paymentOperationHistoryDao.saveRecordOperationHistory(
            order,
            clientSystem,
            traceId,
            ACTION_TYPE_START_PAYMENT_AKB,
        )
        logger.info("$SAVE_OPERATION_HISTORY_START_PAY $ACTION_TYPE_START_PAYMENT_AKB")
        val url = apiConfigProperty.akbUrl
        val pid = requireNotNull(paymentId) { PAYMENT_ID_IS_REQUIRED }
        val urlTuSuccess = urlToReturn ?: apiConfigProperty.backUrlS
        try {
            val listSubOrder = subOrderDao.getAllSubOrderListByOrderId(order, traceId)
            val descAndPremiumAmountData = generatorService.getDescriptionAndPremiumAmount(premiumAmount, listSubOrder)
            val akbRequest = AkbCardAndSbpPaymentRequest(
                buildCardOrderDto(
                    urlToReturn = urlTuSuccess,
                    paymentId = pid,
                    description = descAndPremiumAmountData.description,
                    amount = descAndPremiumAmountData.premiumAmount?.toInt()
                )
            )
            val responseEntity: ResponseEntity<AkbOrderResponse> =
                postJson(url, akbRequest)
            val orderInfo = responseEntity.body?.order
                ?: throw InnerException(traceId, EMPTY_ORDER_RESPONSE)

            val paymentBankId = orderInfo.id ?: 0
            val paymentPageUrl = orderInfo.hppUrl.orEmpty()
            if (paymentPageUrl.isBlank()) {
                logger.error("$EMPTY_HPP_URL_RESPONSE [traceId=$traceId, orderId=$paymentBankId]")
                throw InnerException(traceId, EMPTY_HPP_URL_RESPONSE)
            }
            val result = Response(
                status = StatusEnum.SUCCESS.value,
                code = STATUS_CODE_SUCCESS_PAY_CARD_AKB_BANK,
                traceId = traceId,
                data = DataPay(paymentPageUrl)
            )
            val dataPaymentUpdate = DataPaymentUpdate(
                pid,
                paymentPageUrl,
                "",
                paymentBankId.toString()
            )
            paymentDao.paymentUpdate(dataPaymentUpdate)
            logger.info("$END__METHOD_PAY_BANK_CARD $paymentId")
            logger.info(MESSAGE_INFO_END_AKB_PAYMENT)
            return ResponseEntity.ok(result)
        } catch (e: RestClientException) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order, clientSystem, traceId, ActionType.PAYMENT_START_REQUEST_ERROR_AKB_BANK.value
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR_AKB_BANK)
            logger.error("$ERROR_AKB_PAYMENT_PROCESSING  ${e.message}")
            throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
        } catch (e: Exception) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order, clientSystem, traceId, ActionType.PAYMENT_START_REQUEST_ERROR_AKB_BANK.value
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR_AKB_BANK)
            logger.error("$ERROR_AKB_PAYMENT_PROCESSING ${e.message}")
            throw InnerException(traceId, ERROR_AKB_PAYMENT_PROCESSING_SBP)
        }
    }

    override fun initiateAKBSbpPayment(
        urlToReturn: String?,
        paymentId: Long?,
        premiumAmount: String,
        order: Order,
        subOrder: SubOrder
    ): ResponseEntity<Response<DataPay>> {
        val traceId = getTraceId()
        logger.info("$START_METHOD_PAY_AKB_BANK_SBP $paymentId")
        val clientSystem = subOrder.clientSystem
        val url = apiConfigProperty.akbSbpUrl
        val pid = requireNotNull(paymentId) { PAYMENT_ID_IS_REQUIRED }
        val urlTuSuccess = urlToReturn ?: apiConfigProperty.backUrlS
        try {
            val listSubOrder = subOrderDao.getAllSubOrderListByOrderId(order, traceId)
            val descAndPremiumAmountData = generatorService.getDescriptionAndPremiumAmount(premiumAmount, listSubOrder)
            val expTime = generatorService.nowPlusFormatted(0, 15)
            val akbRequest = AkbCardAndSbpPaymentRequest(
                buildSbpOrderDto(
                    urlToReturn = urlTuSuccess,
                    paymentId = pid,
                    description = descAndPremiumAmountData.description,
                    amount = descAndPremiumAmountData.premiumAmount?.toInt(),
                    expTime = expTime
                )
            )
            val responseEntity: ResponseEntity<AkbOrderResponse> =
                postJson(url, akbRequest)
            val orderInfo = responseEntity.body?.order
                ?: throw InnerException(traceId, EMPTY_ORDER_RESPONSE)
            val paymentBankId = orderInfo.id ?: 0
            val paymentPageUrlToSrcTokenRequest = orderInfo.hppUrl.orEmpty()
            val password = paymentPageUrlToSrcTokenRequest.substringAfter("password=")
            if (paymentPageUrlToSrcTokenRequest.isBlank()) {
                logger.error("$EMPTY_HPP_URL_RESPONSE [traceId=$traceId, orderId=$paymentBankId]")
                throw InnerException(traceId, EMPTY_HPP_URL_RESPONSE)
            }


            setSrcToken(paymentBankId, password, traceId)
            val paymentPageUrl = preparePushTran(paymentBankId, password, traceId)
            val dataPaymentUpdate = DataPaymentUpdate(
                pid,
                paymentPageUrl,
                "",
                paymentBankId.toString()
            )
            paymentDao.paymentUpdate(dataPaymentUpdate)
            val dataPay = DataPay(paymentPageUrl)
            logger.info("$END__METHOD_PAY_AKB_BANK_SBP $paymentId")
            val result = Response(
                status = StatusEnum.SUCCESS.value,
                code = STATUS_CODE_SUCCESS_PAY_CARD_AKB_BANK,
                traceId = traceId,
                data = dataPay
            )
            return ResponseEntity.ok(result)
        } catch (e: RestClientException) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order, clientSystem, traceId, ActionType.PAYMENT_LINK_REQUEST_ERROR.value
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR_AKB_BANK)
            logger.error("$ERROR_AKB_PAYMENT_PROCESSING  ${e.message}")
            throw InnerException(traceId, ERROR_AKB_PAYMENT_PROCESSING + (e.message ?: ""))
        } catch (e: Exception) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order, clientSystem, traceId, ActionType.PAYMENT_LINK_REQUEST_ERROR.value
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR_AKB_BANK)
            logger.error("$ERROR_AKB_PAYMENT_PROCESSING_SBP ${e.message}")
            throw InnerException(traceId, ERROR_AKB_PAYMENT_PROCESSING_SBP + e.message)
        }
    }

    private inline fun <reified T> postJson(
        url: String,
        body: Any
    ): ResponseEntity<T> {
        val entity = HttpEntity(body, jsonHeaders())

        logger.info("AKB request url=$url]: body=${objectMapper.writeValueAsString(body)}")

        val response = restTemplate
            .xpgRestTemplate(props)
            .exchange(url, HttpMethod.POST, entity, object : ParameterizedTypeReference<T>() {})

        logger.info("AKB response url=$url]: status=${response.statusCode}, body=${response.body}")

        return response
    }

    private fun buildCardOrderDto(
        urlToReturn: String,
        paymentId: Long,
        description: String,
        amount: Int?
    ) = OrderDto(
        typeRid = WITH_3DS,
        amount = amount,
        currency = RUB,
        hppRedirectUrl = urlToReturn,
        ridByMerchant = paymentId.toString(),
        adviceIfaceAddress = urlToReturn,
        description = description,
        descriptionHtml = description,
        language = RU
    )

    private fun buildSbpOrderDto(
        urlToReturn: String,
        paymentId: Long,
        description: String,
        amount: Int?,
        expTime: String
    ) = OrderDto(
        typeRid = QRC_PAY,
        amount = amount,
        currency = RUB,
        hppRedirectUrl = urlToReturn,
        ridByMerchant = paymentId.toString(),
        adviceIfaceAddress = urlToReturn,
        description = description,
        descriptionHtml = description,
        language = RU,
        expTime = expTime
    )

    /**
     * Установить SRC-токен для конкретного заказа
     */
    private fun setSrcToken(
        orderId: Int,
        password: String,
        traceId: String
    ) {
        val url = "${apiConfigProperty.akbSbpUrl}/$orderId/$SET_SRC_TOKEN_SUFFIX$password"
        val body = SetSrcTokenRequest(token = mapOf(IPS_RU to true))

        try {
            val response: ResponseEntity<Map<String, Any>> = postJson(url, body)
            logger.info("setSrcToken response.body: ${response.body}")
        } catch (e: Exception) {
            logger.error("Ошибка setSrcToken")
            throw InnerException(traceId, "Ошибка при установке SRC-токена: ${e.message}")
        }
    }

    /**
     * Подготовить push-транзакцию для конкретного заказа
     */
    private fun preparePushTran(
        orderId: Int,
        password: String,
        traceId: String
    ): String? {
        val url = "${apiConfigProperty.akbSbpUrl}/$orderId/$PUSH_TRAN_SUFFIX$password"
        val body = PreparePushTranRequest(
            specificByPm = mapOf(
                IPS_RU to mapOf(REDIRECT_URL to apiConfigProperty.backUrlS)
            )
        )
        return try {
            val response: ResponseEntity<PreparePushTranResponse> =
                restTemplate.xpgRestTemplate(props).exchange(
                    url,
                    HttpMethod.POST,
                    HttpEntity(body),
                    object : ParameterizedTypeReference<PreparePushTranResponse>() {}
                )

            logger.info("preparePushTran response.body: ${response.body}")

            response.body
                ?.specificByPm
                ?.get(IPS_RU)
                ?.qrcPayload
        } catch (e: Exception) {
            logger.error("Ошибка получения qrcPayload:  ${e.message}")
            throw InnerException(traceId, "Ошибка при подготовке push-транзакции: ${e.message}")
        }
    }

    private fun jsonHeaders(): HttpHeaders = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }
}
