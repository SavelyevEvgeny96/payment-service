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
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.GPBPaymentRequest
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
import ru.sogaz.site.paymentService.util.Util
import ru.sogaz.siter.models.resonses.Response

class GazpromServiceImpl(
    private val getActionTypeDao: GetActionTypeDao,
    private val paymentRepository: PaymentRepository,
    private val apiConfigProperty: ApiConfigProperties,
    private val objectMapper: ObjectMapper,
    private val subOrderRepository: SubOrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val restTemplate: WebConfigRestTemplate,
    private val util: Util,
    private val getPaymentStatusDao: GetPaymentStatusDao,
    private val getPaymentDao: GetPaymentDao,
) : GazpromService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val PAYMENT_STATUS_REG = "REG"
        const val SAVE_OPERATION_HISTORY_START_PAY_ERROR =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа"
        const val SAVE_OPERATION_HISTORY_START_PAY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY запрос на старт платежа"
        const val SAVE_OPERATION_HISTORY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY при не удачном получении GPB Token "
        const val GET_TOKEN_MASSAGE_FAIL = "Ошибка при получении токена доступа"
        const val SENDING_REQUEST_START_PAY = "Отправка запроса для старта платежа"
        const val ERROR_SENDING_REQUEST_START_PAY = "Ошибка при отправке запроса на старт платежа"
        const val ERROR_GPB_PAYMENT_PROCESSING = "Ошибка обработки платежа GPB для TraceId: "
        const val PAYMENT_PAGE_URL = "paymentPageUrl"
        const val OPTIONS = "options"
        const val LOG_ERROR_GET_TOKEN = "Ошибка получения токена доступа от GPB , система не доступна для TraceId: {}"
        const val LOG_SUCCESSFUL_GPB_API = "Успешный запрос к GPB API. TraceId: "
        const val PAYMENT_PAGE = "payment_page"
    }

    override fun getGPBToken(
        traceId: String,
        order: Order,
        subOrder: SubOrder,
    ): String {
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.TOKEN_PREFIX}"
        try {
            val response: ResponseEntity<String> =
                restTemplate.restTemplate().exchange(url, HttpMethod.POST, null, String::class.java)
            val jsonResponse = objectMapper.readTree(response.body)
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
            logger.info(SAVE_OPERATION_HISTORY)
        }
        logger.error(LOG_ERROR_GET_TOKEN + traceId)
        throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
    }

    override fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        premiumAmount: String?,
        order: Order,
        subOrder: SubOrder,
    ): ResponseEntity<Response<DataPay>> {
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
        val description = util.generateDescription(listSubOrder)
        val gpbPaymentRequest =
            GPBPaymentRequest(
                portalId = apiConfigProperty.portalId,
                token = tokenGpb,
                merchantId = apiConfigProperty.merchantId,
                orderId = paymentPayRequest.code,
                backUrlS = urlTuSuccess,
                backUrlF = urlTuReturn,
                amount = fixedAmount,
                description = description,
                currency = PaymentServiceImpl.RUB,
                state =
                    State(
                        redirect = PAYMENT_PAGE,
                    ),
            )
        val requestEntity = HttpEntity(gpbPaymentRequest, headers)
        try {
            val responseEntity: ResponseEntity<Map<String, Any>> =
                restTemplate.restTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<Map<String, Any>>() {},
                )
            logger.info(LOG_SUCCESSFUL_GPB_API, traceId)
            val responseBody = responseEntity.body
            val paymentPageUrl =
                (responseBody?.get(OPTIONS) as? Map<String, Any>)?.get(PAYMENT_PAGE_URL) as? String ?: ""
            val dataPay = DataPay(paymentPageUrl)
            val result =
                Response(
                    status = OrderServiceImpl.SUCCESS,
                    code = OrderServiceImpl.STATUS_CODE_SUCCESS,
                    traceId = traceId,
                    data = dataPay,
                )
            val getPaymentForUpdate = getPaymentDao.getPayment(traceId, order)
            val paymentStatusREG = getPaymentStatusDao.getPaymentStatus(traceId, PAYMENT_STATUS_REG)
            if (getPaymentForUpdate != null) {
                getPaymentForUpdate.paymentPageUrl = paymentPageUrl
                getPaymentForUpdate.stateId = paymentStatusREG
                paymentRepository.save(getPaymentForUpdate)
            }
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
}
