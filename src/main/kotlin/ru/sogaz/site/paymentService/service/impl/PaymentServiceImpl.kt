package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.config.ApiConfig
import ru.sogaz.site.paymentService.dto.Data
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.siter.models.resonses.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
    private val apiConfig: ApiConfig,
    private val bankRepository: BankRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
) : PaymentService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val STATUS_CODE_SUCCESS = 1101500200
        const val SUCCESS = "success"
        const val LOG_START_PAYMENT_CREATION = "Начало создания платежа для TraceId: {}"
        const val LOG_PAYMENT_CREATION_SUCCESS = "Платеж успешно создан с paymentCode: {} для TraceId: {}"
        const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с stateId 0 не найден для TraceId: {}"
        const val LOG_BANK_NOT_FOUND = "Банк не найден, используется по умолчанию для TraceId: {}"
        const val LOG_PAYMENT_ID_GENERATED = "Сгенерирован paymentId: {} для TraceId: {}"
        const val LOG_PAYMENT_CODE_GENERATED = "Сгенерирован paymentCode: {} для TraceId: {}"
        const val LOG_CLIENT_SYSTEM_NOT_FOUND = "Не удалось найти систему клиента для externalSystemCode: {} и TraceId: {}"
        const val LOG_ERROR_WHILE_CREATING_PAYMENT = "Ошибка при создании платежа для TraceId: {}"
        const val ERROR_ORDER_STATUS_NOT_FOUND = "Статус заказа не найден для stateId 0"
        const val ERROR_CLIENT_SYSTEM_NOT_FOUND = "Система клиента не найдена"
        const val ERROR_BANK_NOT_FOUND = "Банк не найден"
        const val ERROR_WHILE_SAVING_ORDER = "Ошибка при сохранении заказа"
    }

    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param paymentRequest Данные о платеже
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект Payment, содержащий информацию о платежном запросе
     */
    override fun createPayment(
        requestWrapper: PaymentRequestWrapper,
        traceId: String,
    ): ResponseEntity<Response<Data>> {
        logger.info(LOG_START_PAYMENT_CREATION, traceId)

        for (paymentRequest in requestWrapper.payments) {
            val result: Response<Data>

            val paymentId = generateUniquePaymentId()
            val paymentCode = generateUniquePaymentCode()
            logger.info(LOG_PAYMENT_ID_GENERATED, paymentId, traceId)
            logger.info(LOG_PAYMENT_CODE_GENERATED, paymentCode, traceId)

            val orderStatus =
                try {
                    orderStatusRepository.findByStateId("0")
                } catch (e: Exception) {
                    logger.error(e, LOG_ORDER_STATUS_NOT_FOUND, traceId)
                    throw IllegalStateException(ERROR_ORDER_STATUS_NOT_FOUND)
                }

            val clientSystem =
                try {
                    clientSystemRepository.findByExternalSystemCode(paymentRequest.externalSystemCode)
                } catch (e: Exception) {
                    logger.error(e, LOG_CLIENT_SYSTEM_NOT_FOUND, paymentRequest.externalSystemCode, traceId)
                    throw IllegalStateException(ERROR_CLIENT_SYSTEM_NOT_FOUND)
                }

            val bank =
                try {
                    if (paymentRequest.bank != null) {
                        bankRepository.findById(paymentRequest.bank.toLong()).orElseThrow()
                    } else {
                        bankRepository.findFirstByOrderById()
                    }
                } catch (e: Exception) {
                    logger.error(e, LOG_BANK_NOT_FOUND, traceId)
                    throw IllegalStateException(ERROR_BANK_NOT_FOUND)
                }

            val paymentPageUrl = "${apiConfig.paymentUrl}$paymentCode}"

            val order =
                Order(
                    paymentId = paymentId,
                    code = paymentCode,
                    bank = bank,
                    premiumAmount = paymentRequest.premiumAmount,
                    recipientEmail = paymentRequest.recipientEmail,
                    recipientPhone = paymentRequest.recipientPhone,
                    operationId = paymentRequest.operationId,
                    paymentEndDate = paymentRequest.paymentEndDate,
                    clientSystem = clientSystem,
                    docType = paymentRequest.docType,
                    policyId = paymentRequest.policyId,
                    policyNumber = paymentRequest.policyNumber,
                    contractId = paymentRequest.contractId,
                    needReceipt = paymentRequest.needReceipt,
                    policyholder = paymentRequest.policyHolder,
                    policyholderDoc = paymentRequest.policyHolderDoc,
                    managerEmail = paymentRequest.managerEmail,
                    urlToReturn = paymentRequest.urlToReturn,
                    urlToDecline = paymentRequest.urlToDecline,
                    customURL = paymentRequest.customURL,
                    paymentPageUrl = paymentPageUrl,
                    hash = paymentRequest.hash,
                    createDate = getCurrentDateMoscow(),
                    updateDate = getCurrentDateMoscow(),
                    orderStatus = orderStatus,
                    typeInsurance = paymentRequest.typeInsurance,
                    contractNumber = paymentRequest.contractNumber,
                    insuranceProgram = paymentRequest.insuranceProgram,
                    recipientUserId = paymentRequest.recipientUserId,
                    dateDelete = null,
                )

            try {
                orderRepository.save(order)
                logger.info(LOG_PAYMENT_CREATION_SUCCESS, paymentCode, traceId)

                val data = Data(paymentCode, paymentPageUrl)
                result =
                    Response(
                        status = SUCCESS,
                        code = STATUS_CODE_SUCCESS,
                        traceId = traceId,
                        data = data,
                    )
                return ResponseEntity(result, HttpStatus.OK)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_WHILE_CREATING_PAYMENT, traceId)
                throw IllegalStateException(ERROR_WHILE_SAVING_ORDER)
            }
        }
        throw IllegalStateException("Ошибка при обработке платежей")
    }
}

private fun generateUniquePaymentCode(): String =
    UUID
        .randomUUID()
        .toString()
        .replace("-", "")
        .take(6)
        .uppercase(Locale.getDefault())

fun getCurrentDateMoscow(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000")
    sdf.timeZone = TimeZone.getTimeZone("Europe/Moscow")
    return sdf.format(Date())
}

private fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
