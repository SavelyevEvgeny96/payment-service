package ru.sogaz.site.paymentService.dao.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_FIND_ACTION_TYPE
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_AND_ERROR_GET_PAYMENT_STATUS
import ru.sogaz.siter.models.resonses.Response
import java.util.Locale
import java.util.UUID

open class ConfigDataDaoImpl(
    private val paymentStatusRepository: PaymentStatusRepository,
    private val paymentRepository: PaymentRepository,
    private val apiConfigProperty: ApiConfigProperty,
    private val configDataRepository: ConfigDataRepository,
    private val objectMapper: ObjectMapper,
    private val restTemplate: WebConfigRestTemplate,
    private val bankRepository: BankRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val subOrderRepository: SubOrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
) : ConfigDataDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val DESC = "Оплата: "
        const val LOG_ERROR_GET_PAYMENT_BY_ORDER_ID = "Не найден платеж по данному TraceId: "
        const val PAYMENT_STATUS_REG = "REG"
        const val SAVE_OPERATION_HISTORY_START_PAY_ERROR =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY ошибка запроса на старт платежа"
        const val SAVE_OPERATION_HISTORY_START_PAY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY запрос на старт платежа"
        const val ERROR_GET_PAYMENT_BY_ORDER_ID = "Не найден платеж по данному order_id: "
        const val SAVE_OPERATION_HISTORY =
            "Добавлена запись в таблицу PAYMENT_OPERATION_HISTORY при не удачном получении GPB Token "
        const val GET_TOKEN_MASSAGE_FAIL = "Ошибка при получении токена доступа"
        const val SENDING_REQUEST_START_PAY = "Отправка запроса для старта платежа"
        const val ERROR_SENDING_REQUEST_START_PAY = "Ошибка при отправке запроса на старт платежа"
        const val ERROR_GPB_PAYMENT_PROCESSING = "Ошибка обработки платежа GPB для TraceId: "
        const val CODE_LENGTH = "codeLength"
        const val BANK_PRIORITY = "bankPriority"
        const val PAYMENT_PAGE_URL = "paymentPageUrl"
        const val OPTIONS = "options"
        const val LOG_ERROR_GET_TOKEN = "Ошибка получения токена доступа от GPB , система не доступна для TraceId: {}"
        const val LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND = "Не найдено значение bankPriority"
        const val LOG_CODE_LENGTH_NOT_FOUND = "Не найдено значение длины для генерации Order.code "
        const val ERROR_ORDER_CODE_LENGTH_NOT_FOUND = "Длина кода не найдена"
        const val LOG_SUCCESSFUL_GPB_API = "Успешный запрос к GPB API. TraceId: "
    }

    override fun getCodeLength(traceId: String): Int {
        val config =
            try {
                configDataRepository.findByParamName(CODE_LENGTH)
            } catch (e: Exception) {
                logger.error(e, LOG_CODE_LENGTH_NOT_FOUND)
                throw InnerException(traceId, ERROR_ORDER_CODE_LENGTH_NOT_FOUND)
            }
        return config.paramValue.toIntOrNull() ?: 6
    }

    override fun generateUniquePaymentCode(traceId: String): String {
        val codeLength = getCodeLength(traceId)

        return UUID
            .randomUUID()
            .toString()
            .replace("-", "")
            .take(codeLength)
            .uppercase(Locale.getDefault())
    }

    override fun getBankPriority(traceId: String): String {
        val config =
            try {
                configDataRepository.findByParamName(BANK_PRIORITY)
            } catch (e: Exception) {
                logger.error(e, LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND)
                throw InnerException(traceId, LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND)
            }
        return config.paramValue
    }

    override fun getBank(
        bankId: String?,
        traceId: String,
    ): Bank? {
        val bank =
            if (bankId.isNullOrBlank()) {
                val reserveConfigBank = getBankPriority(traceId)
                bankRepository.findByBankId(reserveConfigBank)
            } else {
                bankRepository.findByBankId(bankId)
            }
        return bank
    }

    override fun getGPBToken(
        traceId: String,
        order: Order,
    ): String {
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.TOKEN_PREFIX}"
        try {
            val response: ResponseEntity<String> =
                restTemplate.restTemplate().exchange(url, HttpMethod.POST, null, String::class.java)
            val jsonResponse = objectMapper.readTree(response.body)
            return jsonResponse.get(PaymentServiceImpl.GPB_TOKEN_ROW).asText().toString()
        } catch (e: Exception) {
            val actionTypeTokenFail =
                try {
                    actionTypeRepository.findByActionName(GET_TOKEN_MASSAGE_FAIL)
                } catch (e: Exception) {
                    logger.error(e, LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
                    throw InnerException(traceId, LOG_AND_ERROR_FIND_ACTION_TYPE)
                }
            val subOrder =
                try {
                    subOrderRepository.findFirstByOrderId(order)
                } catch (e: Exception) {
                    logger.error(e, PaymentServiceImpl.LOG_AND_ERROR_FIND_SUB_ORDER, order.code)
                    throw InnerException(traceId, PaymentServiceImpl.LOG_AND_ERROR_FIND_SUB_ORDER)
                }
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
        throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
    }

    override fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        premiumAmount: String?,
        order: Order,
    ): ResponseEntity<Response<DataPay>> {
        val actionTypeStartPay =
            try {
                actionTypeRepository.findByActionName(SENDING_REQUEST_START_PAY)
            } catch (e: Exception) {
                logger.error(e, LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
                throw InnerException(traceId, LOG_AND_ERROR_FIND_ACTION_TYPE)
            }
        val subOrder =
            try {
                subOrderRepository.findFirstByOrderId(order)
            } catch (e: Exception) {
                logger.error(e, PaymentServiceImpl.LOG_AND_ERROR_FIND_SUB_ORDER, order.code)
                throw InnerException(traceId, PaymentServiceImpl.LOG_AND_ERROR_FIND_SUB_ORDER)
            }
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

        val fixedAmount = premiumAmount?.replace(".", "")
        val listSubOrder = subOrderRepository.findAllByOrderId(order)
        println(listSubOrder)
        val gpbPaymentRequest =
            GPBPaymentRequest(
                portalId = apiConfigProperty.portalId,
                token = tokenGpb,
                merchantId = apiConfigProperty.merchantId,
                orderId = paymentPayRequest.code,
                backUrlS = apiConfigProperty.backUrlS,
                backUrlF = apiConfigProperty.backUrlF,
                amount = fixedAmount,
                //сделать как в спеке
                description = DESC + listSubOrder,
                currency = PaymentServiceImpl.RUB,
                returnUrl = apiConfigProperty.returnUrl,
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
            val getPaymentForUpdate =
                try {
                    paymentRepository.findByOrderId(order)
                } catch (e: Exception) {
                    logger.error(e, LOG_ERROR_GET_PAYMENT_BY_ORDER_ID, traceId)
                    throw InnerException(traceId, "${ERROR_GET_PAYMENT_BY_ORDER_ID}$order")
                }
            val paymentStatusREG =
                try {
                    paymentStatusRepository.findByStateId(PAYMENT_STATUS_REG)
                } catch (e: Exception) {
                    logger.error(e, LOG_AND_ERROR_GET_PAYMENT_STATUS, traceId)
                    throw InnerException(traceId, LOG_AND_ERROR_GET_PAYMENT_STATUS)
                }

            getPaymentForUpdate.paymentPageUrl = paymentPageUrl
            getPaymentForUpdate.stateId = paymentStatusREG
            paymentRepository.save(getPaymentForUpdate)
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            val actionTypeStartPayError =
                try {
                    actionTypeRepository.findByActionName(ERROR_SENDING_REQUEST_START_PAY)
                } catch (e: Exception) {
                    logger.error(e, LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
                    throw InnerException(traceId, LOG_AND_ERROR_FIND_ACTION_TYPE)
                }
            val operationHistoryError =
                PaymentOperationHistory(
                    action = actionTypeStartPayError,
                    order = order,
                    actionAuthor = subOrder.clientSystem,
                    actionDate = null,
                )
            operationHistoryRepository.save(operationHistoryError)
            logger.info(SAVE_OPERATION_HISTORY_START_PAY_ERROR)
            logger.error(e, ERROR_GPB_PAYMENT_PROCESSING + "$traceId")
            throw InnerException(traceId, ERROR_GPB_PAYMENT_PROCESSING)
        }
    }

    override fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
}
