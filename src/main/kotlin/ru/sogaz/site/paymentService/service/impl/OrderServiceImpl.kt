package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dto.DataOrder
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import java.math.BigDecimal
import java.math.RoundingMode

class OrderServiceImpl(
    private val configDataRepository: ConfigDataRepository,
    private val configDataDao: ConfigDataDao,
    private val apiConfigProperty: ApiConfigProperty,
    private val bankRepository: BankRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val subOrderRepository: SubOrderRepository,
) : OrderService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val STATE_ID_NEW = "NEW"
        const val PREMIUM_FORMAT = "%.2f"
        const val LOG_ORDER_UPDATED_WITH_PREMIUM = "Обновление общей суммы премии"
        const val STATUS_CODE_SUCCESS = 1101500200
        const val SUCCESS = "success"
        const val LOG_START_ORDER_CREATION = "Начало создания заявки для TraceId: {}"
        const val LOG_ORDER_CREATION_SUCCESS = "Заказ успешно создан с orderCode: {} для TraceId: {}"
        const val LOG_SUB_ORDER_CREATION_SUCCESS = "Подзаказ успешно создан с paymentCode: {} для TraceId: {}"
        const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с stateId 0 не найден для TraceId: {}"
        const val LOG_ERROR_WHILE_UPDATING_ORDER = "Ошибка при обновлении суммы премии заказа"
        const val LOG_BANK_NOT_FOUND = "Банк не найден, используется по умолчанию для TraceId: {}"
        const val LOG_PAYMENT_ID_GENERATED = "Сгенерирован orderId: {} для TraceId: {}"
        const val LOG_PAYMENT_CODE_GENERATED = "Сгенерирован paymentCode: {} для TraceId: {}"
        const val LOG_CLIENT_SYSTEM_NOT_FOUND =
            "Не удалось найти систему клиента для externalSystemCode: {} и TraceId: {}"
        const val LOG_ERROR_WHILE_CREATING_ORDER = "Ошибка при создании заказа для TraceId: {}"
        const val LOG_ERROR_WHILE_CREATING_SUB_ORDER = "Ошибка при создании подзаказа для TraceId: {}"
        const val ERROR_ORDER_STATUS_NOT_FOUND = "Статус заказа не найден для stateId 0"
        const val ERROR_CLIENT_SYSTEM_NOT_FOUND = "Система клиента не найдена"
        const val ERROR_BANK_NOT_FOUND = "Банк не найден"
        const val ERROR_WHILE_SAVING_ORDER = "Ошибка при сохранении заказа"
        const val ERROR_WHILE_SAVING_SUB_ORDER = "Ошибка при сохранении подзаказа"
        const val ERROR_WHILE_UPDATING_ORDER = "Ошибка сумма премии не обновленна"
    }

    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param requestWrapper Данные о заказе(содержит внутри лист PaymentRequest)
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект Payment, содержащий информацию о платежном запросе
     */
    override fun createOrder(
        requestWrapper: PaymentRequestWrapper,
        traceId: String,
    ): ResponseEntity<Response<DataOrder>> {
        logger.info(LOG_START_ORDER_CREATION + traceId)
        val orderId = configDataDao.generateUniquePaymentId()
        val orderCode = configDataDao.generateUniquePaymentCode(traceId)
        logger.info(LOG_PAYMENT_ID_GENERATED, orderId, traceId)
        logger.info(LOG_PAYMENT_CODE_GENERATED, orderCode, traceId)

        val requestBankId = requestWrapper.bank

        val bank = if (requestBankId.isNullOrBlank()) {
            val reserveConfigBank = configDataDao.getBankPriority(traceId)
            bankRepository.findByBankId(reserveConfigBank)
        } else {
            bankRepository.findByBankId(requestBankId)
        }

        val orderStatus =
            try {
                orderStatusRepository.findByStateId(STATE_ID_NEW)
            } catch (e: Exception) {
                logger.error(e, LOG_ORDER_STATUS_NOT_FOUND, traceId)
                throw InnerException(traceId, ERROR_ORDER_STATUS_NOT_FOUND)
            }

        val order =
            Order(
                orderId = orderId,
                bankId = bank,
                orderStatus = orderStatus,
                code = orderCode,
                dateDelete = null,
                paymentEndDate = requestWrapper.paymentEndDate,
                customURL = requestWrapper.customURL,
                premiumAmount = null,
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
            throw InnerException(traceId, ERROR_WHILE_SAVING_ORDER)
        }
        var totalPremiumAmount = BigDecimal.ZERO
        for (paymentRequest in requestWrapper.payments) {
            val subOrderId = configDataDao.generateUniquePaymentId()
            logger.info(LOG_PAYMENT_ID_GENERATED, subOrderId, traceId)

            val clientSystem =
                try {
                    clientSystemRepository.findByExternalSystemCode(paymentRequest.externalSystemCode)
                } catch (e: Exception) {
                    logger.error(
                        e,
                        LOG_CLIENT_SYSTEM_NOT_FOUND,
                        paymentRequest.externalSystemCode,
                        traceId,
                    )
                    throw InnerException(traceId, ERROR_CLIENT_SYSTEM_NOT_FOUND)
                }

            val subOrders =
                SubOrder(
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

            paymentRequest.premiumAmount.let {
                totalPremiumAmount = totalPremiumAmount.add(BigDecimal(it))
            }

            try {
                subOrderRepository.save(subOrders)
                logger.info(LOG_SUB_ORDER_CREATION_SUCCESS, subOrderId, traceId)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_WHILE_CREATING_SUB_ORDER, traceId)
                throw InnerException(traceId, ERROR_WHILE_SAVING_SUB_ORDER)
            }
            order.premiumAmount = totalPremiumAmount.setScale(2, RoundingMode.HALF_UP).toString()
            try {
                orderRepository.save(order)
                logger.info(LOG_ORDER_UPDATED_WITH_PREMIUM, orderCode, traceId)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_WHILE_UPDATING_ORDER, traceId)
                throw InnerException(traceId, ERROR_WHILE_UPDATING_ORDER)
            }
        }
        val result: Response<DataOrder>
        val paymentPageUrl = "${apiConfigProperty.paymentUrl}$orderCode"
        try {
            logger.info(LOG_ORDER_CREATION_SUCCESS, orderCode, traceId)
            val dataOrder = DataOrder(orderCode, paymentPageUrl)
            result =
                Response(
                    status = SUCCESS,
                    code = STATUS_CODE_SUCCESS,
                    traceId = traceId,
                    data = dataOrder,
                )
            return ResponseEntity(result, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_WHILE_CREATING_ORDER, traceId)
            throw InnerException(traceId, ERROR_WHILE_SAVING_ORDER)
        }
    }
}
