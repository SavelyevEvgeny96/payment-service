package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dto.DataOrder
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import java.math.BigDecimal
import java.math.RoundingMode

class OrderServiceImpl(
    private val apiConfigProperty: ApiConfigProperties,
    private val getClientSystemDao: GetClientSystemDao,
    private val orderRepository: OrderRepository,
    private val subOrderRepository: SubOrderRepository,
    private val bankDao: BankDao,
    private val generatorService: GeneratorService,
    private val orderStatusDao: OrderStatusDao,
) : OrderService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val STATE_ID_NEW = "NEW"
        const val LOG_ORDER_UPDATED_WITH_PREMIUM = "Обновление общей суммы премии"
        const val STATUS_CODE_SUCCESS = 1101500200
        const val SUCCESS = "SUCCESS"
        const val LOG_START_ORDER_CREATION = "***** Начало ***** создания заявки для TraceId: "
        const val LOG_END_ORDER_CREATION = "***** КОНЕЦ ***** создания заявки для TraceId: "
        const val LOG_ORDER_CREATION_SUCCESS = "Заказ успешно создан и сохранен в базу с orderCode: "
        const val LOG_SUB_ORDER_CREATION_SUCCESS = "Подзаказ успешно создан с subOrderId: "
        const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с stateId 0 не найден для TraceId: "
        const val LOG_ERROR_WHILE_UPDATING_ORDER = "Ошибка при обновлении суммы премии заказа"
        const val LOG_PAYMENT_ID_GENERATED = "Сгенерирован orderId: "
        const val LOG_PAYMENT_SUB_ORDER_ID_GENERATED = "Сгенерирован subOrderId: "
        const val LOG_PAYMENT_CODE_GENERATED = "Сгенерирован paymentCode: "
        const val LOG_ERROR_WHILE_CREATING_ORDER = "Ошибка при создании заказа для TraceId: "
        const val LOG_ERROR_WHILE_CREATING_SUB_ORDER = "Ошибка при создании подзаказа для TraceId: "
        const val ERROR_CLIENT_SYSTEM_NOT_FOUND = "Система клиента не найдена"
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
        val orderId = generatorService.generateUniquePaymentId()
        val orderCode = generatorService.generateUniquePaymentCode(traceId)
        logger.info("$LOG_PAYMENT_ID_GENERATED $orderId")
        logger.info("$LOG_PAYMENT_CODE_GENERATED $orderCode")

        val requestBankId = requestWrapper.bank
        val bank = bankDao.getBank(requestBankId, traceId)
        val orderStatus = orderStatusDao.getOrderStatus(traceId, STATE_ID_NEW)
        val order =
            Order(
                orderId = orderId,
                bankId = bank,
                orderStatus = orderStatus,
                code = orderCode,
                dateDelete = null,
                paymentEndDate = requestWrapper.paymentEndDate,
                premiumAmount = null,
                urlToDecline = requestWrapper.urlToDecline,
                urlToReturn = requestWrapper.urlToReturn,
                recipientEmail = requestWrapper.recipientEmail,
            )

        try {
            orderRepository.save(order)
            logger.info("$LOG_ORDER_CREATION_SUCCESS $orderCode")
        } catch (e: Exception) {
            logger.error(e, "$LOG_ERROR_WHILE_CREATING_ORDER $traceId")
            throw InnerException(traceId, ERROR_WHILE_SAVING_ORDER)
        }
        var totalPremiumAmount = BigDecimal.ZERO
        for (paymentRequest in requestWrapper.payments) {
            val subOrderId = generatorService.generateUniquePaymentId()
            logger.info("$LOG_PAYMENT_SUB_ORDER_ID_GENERATED $subOrderId")
            val clientSystem = getClientSystemDao.getClientSystem(traceId, paymentRequest.externalSystemCode)
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
                logger.info("$LOG_SUB_ORDER_CREATION_SUCCESS $subOrderId")
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_WHILE_CREATING_SUB_ORDER, traceId)
                throw InnerException(traceId, ERROR_WHILE_SAVING_SUB_ORDER)
            }
            order.premiumAmount = totalPremiumAmount.setScale(2, RoundingMode.HALF_UP).toString()
            try {
                orderRepository.save(order)
                logger.info(LOG_ORDER_UPDATED_WITH_PREMIUM)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_WHILE_UPDATING_ORDER, traceId)
                throw InnerException(traceId, ERROR_WHILE_UPDATING_ORDER)
            }
        }
        val result: Response<DataOrder>
        val paymentPageUrl = "${apiConfigProperty.paymentUrl}$orderCode"
        try {
            val dataOrder = DataOrder(orderCode, paymentPageUrl)
            result =
                Response(
                    status = SUCCESS,
                    code = STATUS_CODE_SUCCESS,
                    traceId = traceId,
                    data = dataOrder,
                )
            logger.info("$LOG_END_ORDER_CREATION $traceId")
            return ResponseEntity(result, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e, "$LOG_ERROR_WHILE_CREATING_ORDER $traceId")
            throw InnerException(traceId, ERROR_WHILE_SAVING_ORDER)
        }
    }
}
