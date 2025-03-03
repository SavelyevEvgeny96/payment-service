package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.*
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.siter.models.resonses.Response

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
   private val actionTypeRepository: ActionTypeRepository,
    private val objectMapper: ObjectMapper,
    private val restTemplate: WebConfigRestTemplate,
    private val configDataRepository: ConfigDataRepository,
    private val apiConfigProperty: ApiConfigProperty,
    private val bankRepository: BankRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val subOrderRepository: SubOrderRepository,
) : PaymentService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val GET_TOKEN_MASSAGE_SUCCESS = "Получен токен доступа"
        const val NO = "no"
        const val RU = "ru"
        const val PAYMENT_PAGE = "payment_page"
        const val RUB = "RUB"
        const val PAYMENT_PREFIX = "/payment/"
        const val START_PREFIX = "/start"
        const val TOKEN_PREFIX = "/token"
        const val GPB_TOKEN_ROW = "token"
        const val GPB = "gpb"
        const val TRUE = "true"
        const val BANK_PRIORITY = "bankPriority"
        const val BANK_PRIORITY_CHECK = "bankPriorityCheck"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_OVERDUE = "OVERDUE"
        const val STATUS_MARKEDDEL = "MARKEDDEL"
        const val LOG_START_PAYMENT_CREATION = "Начало создания платежа для TraiceId: {}"
        const val LOG_ERROR_GET_TOKEN = "Ошибка получения токена доступа от GPB , система не доступна для TraceId: {}"
        const val LOG_NOT_FOUND_ORDER_TO_CODE =
            "Ошибка совершения платежа. Указанный заказ (идентификатор/code заказа) не найден"
        const val LOG_ORDER_STATUS_SUCCESS = "Ошибка совершения платежа. Указанный заказ уже оплачен для TraceId: {}"
        const val LOG_ERROR_UPDATE_ORDER_BY_CODE = "Обновление полей urlToReturn и urlToDecline в  заявке не выполненно"
        const val ERROR_UPDATE_ORDER_BY_CODE = "Ошибка Обновления полей urlToReturn и urlToDecline для заявки с code: "
        const val LOG_ERROR_BANK_PRIORITY_CHECK =
            "Параметр с paramName \"bankPriorityCheck\"  не найден в конфигурационной таблице"
        const val LOG_ERROR_BANK_PRIORITY =
            "Параметр с paramName \"bankPriority\"  не найден в конфигурационной таблице"

        const val LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL =
            "Ошибка совершения платежа. Указанный заказ не доступен для оплаты для TraceId: {} "
        const val ERROR_BANK_PRIORITY_CHECK = "Ошибка поиска параметра \"bankPriorityCheck\""
        const val ERROR_BANK_PRIORITY = "Ошибка поиска параметра \"bankPriority\""

    }

    override fun createPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String
    ): ResponseEntity<Response<DataPay>> {
        logger.info(LOG_START_PAYMENT_CREATION + traceId)

        val orderFindByCode = try {
            orderRepository.findByCode(paymentPayRequest.code)
        } catch (e: Exception) {
            logger.error(e, LOG_NOT_FOUND_ORDER_TO_CODE, paymentPayRequest.code, traceId)
            throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND, traceId)
        }
        val premiumAmount = orderFindByCode.premiumAmount
        val orderStatus = orderFindByCode.orderStatus
        if (orderStatus != null) {
            if (orderStatus.stateId == STATUS_SUCCESS) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_SUCCESS + traceId)

                throw BusinessException(CODE_ERROR_ORDER_IS_PAID_FOR, traceId)
            }
            if (orderStatus.stateId == STATUS_OVERDUE || orderStatus.stateId == STATUS_MARKEDDEL)
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL + traceId)

            throw BusinessException(CODE_ERROR_ORDER_IS_NOT_AVAILABLE, traceId)
        }
        orderFindByCode.urlToReturn = paymentPayRequest.urlToReturn
        orderFindByCode.urlToDecline = paymentPayRequest.urlToReturnF

        try {
            orderRepository.save(orderFindByCode)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_UPDATE_ORDER_BY_CODE, traceId)
            throw InnerException(traceId, ERROR_UPDATE_ORDER_BY_CODE + orderFindByCode.code)
        }

        val configBankPriority =
            try {
                configDataRepository.findByParamName(BANK_PRIORITY)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_BANK_PRIORITY, traceId)
                throw InnerException(traceId, ERROR_BANK_PRIORITY)
            }
        val configBankPriorityCheck =
            try {
                configDataRepository.findByParamName(BANK_PRIORITY_CHECK)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_BANK_PRIORITY_CHECK, traceId)
                throw InnerException(traceId, ERROR_BANK_PRIORITY_CHECK)
            }
        if (configBankPriorityCheck.paramValue == TRUE) {
            if (configBankPriority.paramValue == GPB) {
                val tokenGpb = getGPBToken(traceId)
                if (tokenGpb.isNotEmpty()){
                    val actionTypeTokenSuccess=
                        try {
                            actionTypeRepository.findByActionName(GET_TOKEN_MASSAGE_SUCCESS)
                        } catch (e: Exception) {
                            logger.error(e, , traceId)
                            throw InnerException(traceId, )
                        }
val operationHistory = PaymentOperationHistory(
    action = actionTypeTokenSuccess,
    order = orderFindByCode,
   actionAuthor = ,
    actionDate =


)
                }
                initiateGPBPayment(paymentPayRequest, traceId, tokenGpb,premiumAmount)
            } else {

            }
        }

        return ResponseEntity.ok

    }

    override fun getGPBToken(traceId: String): String {
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}$TOKEN_PREFIX"
        try {
            val response: ResponseEntity<String> =
                restTemplate.restTemplate().exchange(url, HttpMethod.POST, null, String::class.java)
            val jsonResponse = objectMapper.readTree(response.body)
            return jsonResponse.get(GPB_TOKEN_ROW).asText().toString()
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_GET_TOKEN + traceId)
            throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
        }
    }

    override fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        premiumAmount:String?
    ): ResponseEntity<Response<DataPay>> {
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}$PAYMENT_PREFIX${tokenGpb}$START_PREFIX"

        val gpbPaymentRequest = GPBPaymentRequest(
            supported3ds = true,
            portalId = apiConfigProperty.portalId,
            token = tokenGpb,
            merchantId = apiConfigProperty.merchantId,
            orderId = paymentPayRequest.code,
            backUrlS = apiConfigProperty.backUrlS,
            backUrlF = apiConfigProperty.backUrlF,
            amount = premiumAmount,
            description = "Оплата для заказа с code{} : " + paymentPayRequest.code,
            currency = RUB,
            lang = RU,
            stateInProgress = NO,
            stateRedirect = PAYMENT_PAGE,
        )


        val response = restTemplate.restTemplate().postForEntity(url, gpbPaymentRequest, String::class.java)

    }
}