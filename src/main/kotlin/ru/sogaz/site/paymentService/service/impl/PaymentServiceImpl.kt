package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.config.ApiConfig
import ru.sogaz.site.paymentService.dto.Data
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.*
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
    private val configDataRepository: ConfigDataRepository,
    private val apiConfig: ApiConfig,
    private val bankRepository: BankRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val subOrderRepository: SubOrderRepository,
) : PaymentService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_ORDER_UPDATED_WITH_PREMIUM = "Обновление общей суммы премии"
        const val STATUS_CODE_SUCCESS = 1101500200
        const val SUCCESS = "success"
        const val LOG_START_PAYMENT_CREATION = "Начало создания платежа для TraceId: {}"
        const val LOG_ORDER_CREATION_SUCCESS = "Заказ успешно создан с orderCode: {} для TraceId: {}"
        const val LOG_SUB_ORDER_CREATION_SUCCESS = "Подзаказ успешно создан с paymentCode: {} для TraceId: {}"
        const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с stateId 0 не найден для TraceId: {}"
        const val LOG_ERROR_WHILE_UPDATING_ORDER = "Ошибка при обновлении суммы премии заказа"
        const val LOG_BANK_NOT_FOUND = "Банк не найден, используется по умолчанию для TraceId: {}"
        const val LOG_CODE_LENGTH_NOT_FOUND = "Не найдено значение длины для генерации Order.code "
        const val LOG_PAYMENT_ID_GENERATED = "Сгенерирован orderId: {} для TraceId: {}"
        const val LOG_PAYMENT_CODE_GENERATED = "Сгенерирован paymentCode: {} для TraceId: {}"
        const val LOG_CLIENT_SYSTEM_NOT_FOUND =
            "Не удалось найти систему клиента для externalSystemCode: {} и TraceId: {}"
        const val LOG_ERROR_WHILE_CREATING_ORDER = "Ошибка при создании заказа для TraceId: {}"
        const val LOG_ERROR_WHILE_CREATING_SUB_ORDER = "Ошибка при создании подзаказа для TraceId: {}"
        const val ERROR_ORDER_STATUS_NOT_FOUND = "Статус заказа не найден для stateId 0"
        const val ERROR_CLIENT_SYSTEM_NOT_FOUND = "Система клиента не найдена"
        const val ERROR_ORDER_CODE_LENGTH_NOT_FOUND = "Длина кода не найдена"
        const val ERROR_BANK_NOT_FOUND = "Банк не найден"
        const val ERROR_WHILE_SAVING_ORDER = "Ошибка при сохранении заказа"
        const val ERROR_WHILE_SAVING_SUB_ORDER = "Ошибка при сохранении подзаказа"
        const val ERROR_WHILE_UPDATING_ORDER = "Ошибка сумма премии не обновленна"
    }

    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param paymentRequest Данные о платеже
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект Payment, содержащий информацию о платежном запросе
     */
    override fun createOrder(
        requestWrapper: PaymentRequestWrapper,
        traceId: String,
    ): ResponseEntity<Response<Data>> {
        logger.info(LOG_START_PAYMENT_CREATION, traceId)
        val orderId = generateUniquePaymentId()
        val orderCode = generateUniquePaymentCode()
        logger.info(LOG_PAYMENT_ID_GENERATED, orderId, traceId)
        logger.info(LOG_PAYMENT_CODE_GENERATED, orderCode, traceId)

        val bank = try {
            bankRepository.findByBankId(requestWrapper.bank).orElseThrow()
        } catch (e: Exception) {
            logger.error(e, LOG_BANK_NOT_FOUND, traceId)
            throw IllegalStateException(ERROR_BANK_NOT_FOUND)
        }

        val orderStatus = try {
            orderStatusRepository.findByStateId("NEW")
        } catch (e: Exception) {
            logger.error(e, LOG_ORDER_STATUS_NOT_FOUND, traceId)
            throw IllegalStateException(ERROR_ORDER_STATUS_NOT_FOUND)
        }

        val order = Order(
            orderId = orderId,
            bankId = bank,
            orderStatus = orderStatus,
            code = orderCode,
            createDate = getCurrentDateMoscow(),
            dateDelete = null,
            paymentEndDate = requestWrapper.paymentEndDate,
            customURL = requestWrapper.customURL,
            premiumAmount = null,  // На данный момент не установлено, мы это установим позже
            updateDate = getCurrentDateMoscow(),
            urlToDecline = requestWrapper.urlToDecline,
            urlToReturn = requestWrapper.urlToReturn,
            recipientUserId = requestWrapper.recipientUserId,
            recipientEmail = requestWrapper.recipientEmail,
            recipientPhone = requestWrapper.recipientPhone,
            needReceipt = requestWrapper.needReceipt,
            policyholder = requestWrapper.policyHolder,
            policyholderDoc = requestWrapper.policyHolderDoc,
        )

        try {
            orderRepository.save(order)
            logger.info(LOG_ORDER_CREATION_SUCCESS, orderCode, traceId)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_WHILE_CREATING_ORDER, traceId)
            throw IllegalStateException(ERROR_WHILE_SAVING_ORDER)
        }

        var totalPremiumAmount: Double = 0.0

        for (paymentRequest in requestWrapper.payments) {
            val subOrderId = generateUniquePaymentId()
            logger.info(LOG_PAYMENT_ID_GENERATED, subOrderId, traceId)

            val clientSystem = try {
                clientSystemRepository.findByExternalSystemCode(paymentRequest.externalSystemCode)
            } catch (e: Exception) {
                logger.error(e, LOG_CLIENT_SYSTEM_NOT_FOUND, paymentRequest.externalSystemCode, traceId)
                throw IllegalStateException(ERROR_CLIENT_SYSTEM_NOT_FOUND)
            }

            val subOrders = SubOrder(
                subOrderId = subOrderId,
                operationId = paymentRequest.operationId,
                clientSystem = clientSystem,
                docType = paymentRequest.docType,
                policyId = paymentRequest.policyId,
                policyNumber = paymentRequest.policyNumber,
                contractId = paymentRequest.contractId,
                orderId = order,
                managerEmail = paymentRequest.managerEmail,
                hash = paymentRequest.hash,
                typeInsurance = paymentRequest.typeInsurance,
                contractNumber = paymentRequest.contractNumber,
                insuranceProgram = paymentRequest.insuranceProgram,
                premiumAmount = paymentRequest.premiumAmount,
            )

            paymentRequest.premiumAmount?.let {
                totalPremiumAmount += it.toDouble()
            }

            try {
                subOrderRepository.save(subOrders)
                logger.info(LOG_SUB_ORDER_CREATION_SUCCESS, subOrderId, traceId)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_WHILE_CREATING_SUB_ORDER, traceId)
                throw IllegalStateException(ERROR_WHILE_SAVING_SUB_ORDER)
            }
        }

        order.premiumAmount =
            String.format("%.2f", totalPremiumAmount)

        try {
            orderRepository.save(order)
            logger.info(LOG_ORDER_UPDATED_WITH_PREMIUM, orderCode, traceId)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_WHILE_UPDATING_ORDER, traceId)
            throw IllegalStateException(ERROR_WHILE_UPDATING_ORDER)
        }

        val result: Response<Data>
        val paymentPageUrl = "${apiConfig.paymentUrl}$orderCode"
        try {
            logger.info(LOG_ORDER_CREATION_SUCCESS, orderCode, traceId)
            val data = Data(orderCode, paymentPageUrl)
            result = Response(
                status = SUCCESS,
                code = STATUS_CODE_SUCCESS,
                traceId = traceId,
                data = data,
            )
            return ResponseEntity(result, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_WHILE_CREATING_ORDER, traceId)
            throw IllegalStateException(ERROR_WHILE_SAVING_ORDER)
        }
    }


    private fun getCodeLength(): Int {
        val config =
            try {
                configDataRepository.findByParamName("codeLength")
            } catch (e: Exception) {
                logger.error(e, LOG_CODE_LENGTH_NOT_FOUND)
                throw IllegalStateException(ERROR_ORDER_CODE_LENGTH_NOT_FOUND)
            }
        return config?.paramValue?.toIntOrNull() ?: 6
    }

    private fun generateUniquePaymentCode(): String {
        val codeLength = getCodeLength()

        return UUID
            .randomUUID()
            .toString()
            .replace("-", "")
            .take(codeLength)
            .uppercase(Locale.getDefault())
    }
}

fun getCurrentDateMoscow(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000")
    sdf.timeZone = TimeZone.getTimeZone("Europe/Moscow")
    return sdf.format(Date())
}

private fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
