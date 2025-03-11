package ru.sogaz.site.paymentService.dao.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
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
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import ru.sogaz.siter.models.resonses.Response
import java.util.Locale
import java.util.UUID

open class ConfigDataDaoImpl(
    private val apiConfigProperty: ApiConfigProperty,
    private val configDataRepository: ConfigDataRepository,
    private val objectMapper: ObjectMapper,
    private val restTemplate: WebConfigRestTemplate,
    private val bankRepository: BankRepository
) : ConfigDataDao {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND = "Не найдено значение bankPriority"
        const val LOG_CODE_LENGTH_NOT_FOUND = "Не найдено значение длины для генерации Order.code "
        const val ERROR_ORDER_CODE_LENGTH_NOT_FOUND = "Длина кода не найдена"

    }

    override fun getCodeLength(traceId: String): Int {
        val config =
            try {
                configDataRepository.findByParamName("codeLength")
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
                configDataRepository.findByParamName("bankPriority")
            } catch (e: Exception) {
                logger.error(e, LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND)
                throw InnerException(traceId, LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND)
            }
        return config.paramValue
    }

    override fun getBank(bankId: String?,traceId: String): Bank? {
        val bank = if (bankId.isNullOrBlank()) {
            val reserveConfigBank = getBankPriority(traceId)
            bankRepository.findByBankId(reserveConfigBank)
        } else {
            bankRepository.findByBankId(bankId)
        }
        return bank
    }
    override fun getGPBToken(traceId: String): String {
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.TOKEN_PREFIX}"
        try {
            val response: ResponseEntity<String> =
                restTemplate.restTemplate().exchange(url, HttpMethod.POST, null, String::class.java)
            val jsonResponse = objectMapper.readTree(response.body)
            return jsonResponse.get(PaymentServiceImpl.GPB_TOKEN_ROW).asText().toString()
        } catch (e: Exception) {
            logger.error(e, PaymentServiceImpl.LOG_ERROR_GET_TOKEN + traceId)
            throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
        }
    }
    override fun initiateGPBPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
        tokenGpb: String,
        premiumAmount: String?
    ): ResponseEntity<Response<DataPay>> {
        val url = "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.PAYMENT_PREFIX}${tokenGpb}${PaymentServiceImpl.START_PREFIX}"

        val gpbPaymentRequest = GPBPaymentRequest(
            supported3ds = true,
            portalId = apiConfigProperty.portalId,
            token = tokenGpb,
            merchantId = apiConfigProperty.merchantId,
            orderId = paymentPayRequest.code,
            backUrlS = apiConfigProperty.backUrlS,
            backUrlF = apiConfigProperty.backUrlF,
            amount = premiumAmount,
            description = PaymentServiceImpl.DESC + paymentPayRequest.code,
            currency = PaymentServiceImpl.RUB,
            lang = PaymentServiceImpl.RU,
            stateInProgress = PaymentServiceImpl.NO,
            stateRedirect = PaymentServiceImpl.PAYMENT_PAGE
        )

        val responseEntity: ResponseEntity<Map<String, Any>> =
            restTemplate.restTemplate().exchange(
                url,
                HttpMethod.POST,
                HttpEntity(gpbPaymentRequest),
                object : ParameterizedTypeReference<Map<String, Any>>() {}
            )
        val responseBody = responseEntity.body
        val paymentPageUrl = responseBody?.get("paymentPageUrl") as? String ?: ""

        val dataPay = DataPay(paymentPageUrl)
        val result: Response<DataPay> = Response(
            status = OrderServiceImpl.SUCCESS,
            code = OrderServiceImpl.STATUS_CODE_SUCCESS,
            traceId = traceId,
            data = dataPay,
        )

        return ResponseEntity.ok(result)
    }

    override fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
}
