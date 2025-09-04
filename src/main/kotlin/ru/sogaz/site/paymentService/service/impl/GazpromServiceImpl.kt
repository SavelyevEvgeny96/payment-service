package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
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
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.RUB
import ru.sogaz.siter.models.resonses.Response
import java.time.Duration
import java.time.Instant

class GazpromServiceImpl(
    private val generatorService: GeneratorService,
    private val apiConfigProperty: ApiConfigProperties,
    private val objectMapper: ObjectMapper,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val restTemplate: WebConfigRestTemplate,
    private val paymentDao: PaymentDao,
    private val subOrderDao: SubOrderDao,
) : GazpromService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val PAYMENT_STATUS_REG = "REG"
        const val TEMPLATE_VERSION = "01"
        const val QR_TTL = "60"
        const val QR_TYPE = "02"
        const val QRC_ID = "qrcId"
        const val PAYMENT_PREFIX = "/payment/"
        const val START_PREFIX = "/start"
        const val SAVE_OPERATION_HISTORY_START_PAY_SBP_ERROR =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа по СБП ГАЗПРОМ БАНК"
        const val SAVE_OPERATION_HISTORY_START_PAY_ERROR =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа ГАЗПРОМ БАНК "
        const val STATUS_CODE_SUCCESS_PAY_SBP = 1101530200
        const val STATUS_CODE_SUCCESS_PAY_CARD_GAZPROM = 1101510200
        const val START_METHOD_PAY_BANK_CARD = ">>> СТАРТ метода оплата картой Газпром Банк для платежа с payment_id: "
        const val END__METHOD_PAY_BANK_CARD = "<<< КОНЕЦ  метода оплата картой Газпром Банк для платежа с payment_id: "
        const val START_METHOD_PAY_BANK_SBP = ">>> СТАРТ метода оплата по СБП Газпром Банк для платежа с payment_id: "
        const val END__METHOD_PAY_BANK_SBP = "<<< КОНЕЦ  метода оплата по СБП Газпром Банк для платежа с payment_id: "
        const val START_METHOD_GET_TOKEN_GPB =
            ">>> СТАРТ метода получение токена из банка Газпром для" +
                " инициирования оплаты по карте для order_id: "
        const val END_METHOD_GET_TOKEN_GPB =
            "<<< КОНЕЦ метода получение токена из банка Газпром для" +
                " инициирования оплаты по карте для order_id: "
        const val SAVE_OPERATION_HISTORY_START_PAY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY запрос на: "
        const val ERROR_UPDATE_PAYMENT_RECORD = "Ошибка обновления платежа payment_id == null"
        const val ERROR_SAVE_OPERATION_HISTORY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY при не удачном получении GPB Token "
        const val ERROR_GPB_PAYMENT_PROCESSING = "Ошибка обработки платежа GPB "
        const val PAYMENT_PAGE_URL = "paymentPageUrl"
        const val PAYLOAD = "payload"
        const val OPTIONS = "options"
        const val DATA = "data"
        const val TRANSACTION_ID = "transactionId"
        const val LOG_ERROR_GET_TOKEN = "Ошибка получения токена доступа от GPB , система не доступна для TraceId: "
        const val LOG_SUCCESSFUL_GPB_API = "Успешный запрос к GPB API."
        const val LOG_SUCCESSFUL_GPB_API_SBP = "Успешный запрос к GPB API по SBP. TraceId: "
        const val PAYMENT_PAGE = "payment_page"
    }

    override fun getGPBToken(
        order: Order,
        subOrder: SubOrder,
    ): String {
        val traceId = getTraceId()
        logger.info("$START_METHOD_GET_TOKEN_GPB ${order.orderId}")
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.TOKEN_PREFIX}"
        try {
            val response: ResponseEntity<String> =
                restTemplate.defaultRestTemplate().exchange(url, HttpMethod.POST, null, String::class.java)
            val jsonResponse = objectMapper.readTree(response.body)
            logger.info("$END_METHOD_GET_TOKEN_GPB ${order.orderId}")
            return jsonResponse.get(PaymentServiceImpl.GPB_TOKEN_ROW).asText().toString()
        } catch (e: Exception) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                subOrder.clientSystem,
                traceId,
                ActionType.GET_ACCESS_TOKEN_ERROR.value,
            )
            logger.info(ERROR_SAVE_OPERATION_HISTORY)
        }
        logger.error("$LOG_ERROR_GET_TOKEN $traceId")
        throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
    }

    override fun initiateGPBPayment(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
        tokenGpb: String,
        paymentId: Long?,
        premiumAmount: String,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>> {
        val traceId = getTraceId()
        logger.info("$START_METHOD_PAY_BANK_CARD $paymentId")
        val clientSystem = subOrder.clientSystem

        paymentOperationHistoryDao.saveRecordOperationHistory(
            order,
            clientSystem,
            traceId,
            ActionType.SEND_PAYMENT_START_REQUEST.value,
        )

        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PAYMENT_PREFIX}$tokenGpb${START_PREFIX}"

        val urlSuccess = urlToReturn ?: apiConfigProperty.backUrlS
        val urlFail = urlToReturnF ?: apiConfigProperty.backUrlF

        val listSubOrder = subOrderDao.getAllSubOrderListByOrderId(order, traceId)
        val descAndPremiumAmountData = generatorService.getDescriptionAndPremiumAmount(premiumAmount, listSubOrder)

        val gpbPaymentRequest =
            buildGpbCardOrderDto(
                urlToReturn = urlSuccess,
                urlToReturnF = urlFail,
                orderId = orderId,
                tokenGpb = tokenGpb,
                description = descAndPremiumAmountData.description,
                amount = descAndPremiumAmountData.premiumAmount,
            )
        return try {
            val response = postJsonGpb<Map<String, Any>>(url, gpbPaymentRequest)
            logger.info(LOG_SUCCESSFUL_GPB_API, traceId)

            val responseBody = response.body
            val paymentPageUrl = (responseBody?.get(OPTIONS) as? Map<*, *>)?.get(PAYMENT_PAGE_URL) as? String ?: ""

            val dataPay = DataPay(paymentPageUrl)
            val result =
                Response(
                    status = StatusEnum.SUCCESS.value,
                    code = STATUS_CODE_SUCCESS_PAY_CARD_GAZPROM,
                    traceId = traceId,
                    data = dataPay,
                )
            val dataPaymentUpdate = DataPaymentUpdate(paymentId, paymentPageUrl, "", tokenGpb)
            paymentDao.paymentUpdate(dataPaymentUpdate)

            ResponseEntity.ok(result)
        } catch (erst: RestClientException) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                clientSystem,
                traceId,
                ActionType.PAYMENT_START_REQUEST_ERROR.value,
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR)
            logger.error("$ERROR_GPB_PAYMENT_PROCESSING ${erst.message}")
            throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
        } catch (e: Exception) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                clientSystem,
                traceId,
                ActionType.PAYMENT_START_REQUEST_ERROR.value,
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR)
            logger.error("$ERROR_GPB_PAYMENT_PROCESSING ${e.message}")
            throw InnerException(traceId, ERROR_GPB_PAYMENT_PROCESSING)
        }
    }

    override fun initiateGPBSBPPayment(
        paymentId: Long?,
        premiumAmount: String,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>> {
        val traceId = getTraceId()
        logger.info("$START_METHOD_PAY_BANK_SBP $paymentId")
        val clientSystem = subOrder.clientSystem

        paymentOperationHistoryDao.saveRecordOperationHistory(
            order,
            clientSystem,
            traceId,
            ActionType.GET_PAYMENT_LINK.value,
        )

        val listSubOrder = subOrderDao.getAllSubOrderListByOrderId(order, traceId)
        val descAndPremiumAmountData = generatorService.getDescriptionAndPremiumAmount(premiumAmount, listSubOrder)

        val gpbSbpPaymentRequest =
            buildGpbSbpOrderDto(
                description = descAndPremiumAmountData.description,
                amount = descAndPremiumAmountData.premiumAmount,
            )

        return try {
            val response = postJsonGpb<Map<String, Any>>(apiConfigProperty.gpbSbpUrl, gpbSbpPaymentRequest)
            logger.info(LOG_SUCCESSFUL_GPB_API_SBP, traceId)

            val responseBody = response.body
            val qrcId = (responseBody?.get(DATA) as? Map<*, *>)?.get(QRC_ID) as? String ?: ""
            val paymentPageUrl = (responseBody?.get(DATA) as? Map<*, *>)?.get(PAYLOAD) as? String ?: ""
            val transactionId = (responseBody?.get(TRANSACTION_ID) as? Map<*, *>)?.toString() ?: ""

            val dataPay = DataPay(paymentPageUrl)
            val result =
                Response(
                    status = StatusEnum.SUCCESS.value,
                    code = STATUS_CODE_SUCCESS_PAY_SBP,
                    traceId = traceId,
                    data = dataPay,
                )
            val dataPaymentUpdate = DataPaymentUpdate(paymentId, paymentPageUrl, qrcId, transactionId)
            paymentDao.paymentUpdate(dataPaymentUpdate)

            ResponseEntity.ok(result)
        } catch (e: Exception) {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                clientSystem,
                traceId,
                ActionType.PAYMENT_LINK_REQUEST_ERROR.value,
            )
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_SBP_ERROR)
            logger.error(e, ERROR_GPB_PAYMENT_PROCESSING + traceId)
            throw InnerException(traceId, ERROR_GPB_PAYMENT_PROCESSING + e.message)
        }
    }

    private inline fun <reified T> postJsonGpb(
        url: String,
        body: Any,
    ): ResponseEntity<T> {
        val entity = HttpEntity(body, generatorService.jsonHeaders())

        logger.info("GPB request url=$url: body=${objectMapper.writeValueAsString(body)}")

        val start = Instant.now()
        val response =
            restTemplate
                .defaultRestTemplate()
                .exchange(url, HttpMethod.POST, entity, object : ParameterizedTypeReference<T>() {})
        val duration = Duration.between(start, Instant.now())

        logger.info(
            "GPB response url=$url: status=${response.statusCode}, body=${response.body}, took=${generatorService.formatDuration(
                duration,
            )}",
        )

        return response
    }

    private fun buildGpbCardOrderDto(
        urlToReturn: String,
        urlToReturnF: String,
        orderId: String,
        tokenGpb: String,
        description: String,
        amount: String?,
    ) = GPBPaymentRequest(
        portalId = apiConfigProperty.portalId,
        token = tokenGpb,
        merchantId = apiConfigProperty.merchantId,
        orderId = orderId,
        backUrlS = urlToReturn,
        backUrlF = urlToReturnF,
        amount = amount,
        description = description,
        currency = RUB,
        state = State(redirect = PAYMENT_PAGE),
    )

    private fun buildGpbSbpOrderDto(
        description: String,
        amount: String?,
    ) = GPBSBPPaymentRequest(
        amount = amount,
        account = apiConfigProperty.paymentAccount,
        merchantId = apiConfigProperty.merchantIdSbpGpb,
        templateVersion = TEMPLATE_VERSION,
        qrTtl = QR_TTL,
        callbackMerchantNotifications = apiConfigProperty.callbackUrlSbp,
        qrcType = QR_TYPE,
        paymentPurpose = description,
        currency = RUB,
    )
}
