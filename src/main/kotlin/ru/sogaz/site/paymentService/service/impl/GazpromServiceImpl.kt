package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.dto.State
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.SUCCESS
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.RUB
import ru.sogaz.siter.models.resonses.Response

class GazpromServiceImpl(
    private val generatorService: GeneratorService,
    private val getActionTypeDao: GetActionTypeDao,
    private val paymentRepository: PaymentRepository,
    private val apiConfigProperty: ApiConfigProperties,
    private val objectMapper: ObjectMapper,
    private val subOrderRepository: SubOrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val restTemplate: WebConfigRestTemplate,
    private val getPaymentStatusDao: GetPaymentStatusDao,
    private val paymentDao: PaymentDao,
) : GazpromService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val PAYMENT_STATUS_REG = "REG"
        const val TEMPLATE_VERSION = "01"
        const val QR_TTL = "60"
        const val QR_TYPE = "02"
        const val SAVE_OPERATION_HISTORY_START_PAY_SBP_ERROR =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа по СБП ГАЗПРОМ БАНК"
        const val SAVE_OPERATION_HISTORY_START_PAY_ERROR =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа ГАЗПРОМ БАНК "
        const val STATUS_CODE_SUCCESS_PAY_SBP = 1101530200
        const val STATUS_CODE_SUCCESS_PAY_CARD = 1101510200
        const val START_METHOD_PAY_BANK_CARD = ">>> СТАРТ метода оплата картой для платежа с payment_id: "
        const val END__METHOD_PAY_BANK_CARD = "<<< КОНЕЦ  метода оплата картой для платежа с payment_id: "
        const val START_METHOD_PAY_BANK_SBP = ">>> СТАРТ метода оплата по СБП для платежа с payment_id: "
        const val END__METHOD_PAY_BANK_SBP = "<<< КОНЕЦ  метода оплата по СБП для платежа с payment_id: "
        const val START_METHOD_GET_TOKEN_GPB =
            ">>> СТАРТ метода получение токена из банка Газпром для" +
                " инициирования оплаты по карте для order_id: "
        const val END_METHOD_GET_TOKEN_GPB =
            "<<< КОНЕЦ метода получение токена из банка Газпром для" +
                " инициирования оплаты по карте для order_id: "
        const val SAVE_OPERATION_HISTORY_START_PAY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY запрос на старт платежа"
        const val ERROR_UPDATE_PAYMENT_RECORD = "Ошибка обновления платежа payment_id == null"
        const val ERROR_SAVE_OPERATION_HISTORY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY при не удачном получении GPB Token "
        const val GET_TOKEN_MASSAGE_FAIL = "Ошибка при получении токена доступа"
        const val SENDING_REQUEST_START_PAY = "Отправка запроса для старта платежа"
        const val GET_PAYMENT_LINK = "Получение ссылки на оплату"
        const val LOG_MESSAGE_GET_PAYMENT_LINK =
            "Добавлена запись в таблицу история операций на получение ссылки для оплаты  "
        const val ERROR_SENDING_REQUEST_START_PAY = "Ошибка при отправке запроса на старт платежа"
        const val ERROR_SENDING_REQUEST_START_PAY_SBP = "Ошибка при отправке запроса для получения платежной ссылки"
        const val ERROR_GPB_PAYMENT_PROCESSING = "Ошибка обработки платежа GPB для TraceId: "
        const val PAYMENT_PAGE_URL = "paymentPageUrl"
        const val PAYLOAD = "payload"
        const val QRC_ID = "qrcId"
        const val OPTIONS = "options"
        const val DATA = "data"
        const val LOG_ERROR_GET_TOKEN = "Ошибка получения токена доступа от GPB , система не доступна для TraceId: "
        const val LOG_SUCCESSFUL_GPB_API = "Успешный запрос к GPB API. TraceId: "
        const val LOG_SUCCESSFUL_GPB_API_SBP = "Успешный запрос к GPB API по SBP. TraceId: "
        const val PAYMENT_PAGE = "payment_page"
    }

    override fun getGPBToken(
        traceId: String,
        order: Order,
        subOrder: SubOrder,
    ): String {
        logger.info("$START_METHOD_GET_TOKEN_GPB ${order.orderId}")
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.TOKEN_PREFIX}"
        try {
            val response: ResponseEntity<String> =
                restTemplate.restTemplate().exchange(url, HttpMethod.POST, null, String::class.java)
            val jsonResponse = objectMapper.readTree(response.body)
            logger.info("$END_METHOD_GET_TOKEN_GPB ${order.orderId}")
            return jsonResponse.get(PaymentServiceImpl.GPB_TOKEN_ROW).asText().toString()
        } catch (e: Exception) {
            val actionTypeTokenFail = getActionTypeDao.getActionType(traceId, GET_TOKEN_MASSAGE_FAIL)
            val operationHistory =
                PaymentOperationHistory(
                    action = actionTypeTokenFail,
                    order = order,
                    actionAuthor = subOrder.clientSystem,
                    actionDate = null,
                )
            operationHistoryRepository.save(operationHistory)
            logger.info(ERROR_SAVE_OPERATION_HISTORY)
        }
        logger.error("$LOG_ERROR_GET_TOKEN $traceId")
        throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
    }

    override fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        paymentId: Long?,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>> {
        logger.info("$START_METHOD_PAY_BANK_CARD $paymentId")
        val actionTypeStartPay = getActionTypeDao.getActionType(traceId, SENDING_REQUEST_START_PAY)
        val operationHistory =
            PaymentOperationHistory(
                action = actionTypeStartPay,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = null,
            )
        operationHistoryRepository.save(operationHistory)
        logger.info(SAVE_OPERATION_HISTORY_START_PAY)
        val url =
            "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.PAYMENT_PREFIX}${tokenGpb}${PaymentServiceImpl.START_PREFIX}"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val urlTuReturn = paymentPayRequest.urlToReturnF ?: apiConfigProperty.backUrlF
        val urlTuSuccess = paymentPayRequest.urlToReturn ?: apiConfigProperty.backUrlS
        val fixedAmount = premiumAmount?.replace(".", "")
        val listSubOrder = subOrderRepository.findAllByOrderId(order)
        val description = generatorService.generateDescription(listSubOrder)
        val gpbPaymentRequest =
            GPBPaymentRequest(
                portalId = apiConfigProperty.portalId,
                token = tokenGpb,
                merchantId = apiConfigProperty.merchantId,
                orderId = paymentPayRequest.orderId,
                backUrlS = urlTuSuccess,
                backUrlF = urlTuReturn,
                amount = fixedAmount,
                description = description,
                currency = RUB,
                state =
                    State(
                        redirect = PAYMENT_PAGE,
                    ),
            )

        val requestEntity = HttpEntity(gpbPaymentRequest, headers)
        try {
            logger.info(
                "GPB payment Card request [traceId=$traceId]:  body=\n${objectMapper.writeValueAsString(requestEntity.body)}",
            )
            val responseEntity: ResponseEntity<Map<String, Any>> =
                restTemplate.restTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<Map<String, Any>>() {},
                )
            logger.info(LOG_SUCCESSFUL_GPB_API, traceId)
            val responseBody = responseEntity.body
            logger.info(
                "GPB payment Card response [traceId=$traceId]: body=$responseBody",
            )
            val paymentPageUrl =
                (responseBody?.get(OPTIONS) as? Map<String, Any>)?.get(PAYMENT_PAGE_URL) as? String ?: ""
            val dataPay = DataPay(paymentPageUrl)
            val result =
                Response(
                    status = SUCCESS,
                    code = STATUS_CODE_SUCCESS_PAY_CARD,
                    traceId = traceId,
                    data = dataPay,
                )
            paymentUpdate(traceId, paymentId, paymentPageUrl, "")
            logger.info("$END__METHOD_PAY_BANK_CARD $paymentId")
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            val actionTypeStartPayError = getActionTypeDao.getActionType(traceId, ERROR_SENDING_REQUEST_START_PAY)
            val operationHistoryError =
                PaymentOperationHistory(
                    action = actionTypeStartPayError,
                    order = order,
                    actionAuthor = subOrder.clientSystem,
                    actionDate = null,
                )
            operationHistoryRepository.save(operationHistoryError)
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR)
            logger.error(e, ERROR_GPB_PAYMENT_PROCESSING + traceId)
            throw InnerException(traceId, ERROR_GPB_PAYMENT_PROCESSING)
        }
    }

    override fun initiateGPBSBPPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        paymentId: Long?,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>> {
        logger.info("$START_METHOD_PAY_BANK_SBP $paymentId")
        val actionTypeGetLinkPay = getActionTypeDao.getActionType(traceId, GET_PAYMENT_LINK)
        val operationHistory =
            PaymentOperationHistory(
                action = actionTypeGetLinkPay,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = null,
            )
        operationHistoryRepository.save(operationHistory)
        logger.info(LOG_MESSAGE_GET_PAYMENT_LINK)
        val url = apiConfigProperty.gpbSbpUrl
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val fixedAmount = premiumAmount?.replace(".", "")
        val listSubOrder = subOrderRepository.findAllByOrderId(order)
        val description = generatorService.generateDescription(listSubOrder)
        val gpbSbpPaymentRequest =
            GPBSBPPaymentRequest(
                amount = fixedAmount,
                account = apiConfigProperty.paymentAccount,
                merchantId = apiConfigProperty.merchantIdSbpGpb,
                templateVersion = TEMPLATE_VERSION,
                qrTtl = QR_TTL,
                callbackMerchantNotifications = apiConfigProperty.callbackUrlSbp,
                qrcType = QR_TYPE,
                paymentPurpose = description,
                currency = RUB,
            )
        val requestEntity = HttpEntity(gpbSbpPaymentRequest, headers)
        try {
            logger.info(
                "GPB payment SBP request [traceId=$traceId]: body=\n${objectMapper.writeValueAsString(requestEntity.body)}",
            )
            val responseEntity: ResponseEntity<Map<String, Any>> =
                restTemplate.restTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<Map<String, Any>>() {},
                )
            logger.info(LOG_SUCCESSFUL_GPB_API_SBP, traceId)
            val responseBody = responseEntity.body
            logger.info(
                "GPB payment SBP response [traceId=$traceId]: body=$responseBody",
            )
            val qrcId = (responseBody?.get(DATA) as? Map<*, *>)?.get(QRC_ID) as? String ?: ""
            val paymentPageUrl =
                (responseBody?.get(DATA) as? Map<*, *>)?.get(PAYLOAD) as? String ?: ""
            val dataPay = DataPay(paymentPageUrl)
            val result =
                Response(
                    status = SUCCESS,
                    code = STATUS_CODE_SUCCESS_PAY_SBP,
                    traceId = traceId,
                    data = dataPay,
                )
            paymentUpdate(traceId, paymentId, paymentPageUrl, qrcId)
            logger.info("$END__METHOD_PAY_BANK_SBP $paymentId")
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            val actionTypeStartPayError = getActionTypeDao.getActionType(traceId, ERROR_SENDING_REQUEST_START_PAY_SBP)
            val operationHistoryError =
                PaymentOperationHistory(
                    action = actionTypeStartPayError,
                    order = order,
                    actionAuthor = subOrder.clientSystem,
                    actionDate = null,
                )
            operationHistoryRepository.save(operationHistoryError)
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_SBP_ERROR)
            logger.error(e, ERROR_GPB_PAYMENT_PROCESSING + traceId)
            throw InnerException(traceId, ERROR_GPB_PAYMENT_PROCESSING + e.message)
        }
    }

    private fun paymentUpdate(
        traceId: String,
        paymentId: Long?,
        paymentPageUrl: String,
        qtcId: String,
    ) {
        if (paymentId != null) {
            val getPaymentForUpdate = paymentDao.getPayment(traceId, paymentId)
            val paymentStatusREG = getPaymentStatusDao.getPaymentStatus(traceId, PAYMENT_STATUS_REG)
            getPaymentForUpdate?.paymentPageUrl = paymentPageUrl
            getPaymentForUpdate?.stateId = paymentStatusREG
            getPaymentForUpdate?.qrcId = qtcId
            if (getPaymentForUpdate != null) {
                paymentRepository.save(getPaymentForUpdate)
            }
        } else {
            logger.error(ERROR_UPDATE_PAYMENT_RECORD)
            throw InnerException(traceId, ERROR_UPDATE_PAYMENT_RECORD)
        }
    }
}
