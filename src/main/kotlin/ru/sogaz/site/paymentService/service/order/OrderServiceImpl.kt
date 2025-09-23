package ru.sogaz.site.paymentService.service.order

import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.PaymentRequest
import ru.sogaz.site.paymentService.dto.request.PaymentRequestWrapper
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZonedDateTime
import java.util.UUID

class OrderServiceImpl(
    private val apiConfigProperty: ApiConfigProperties,
    private val orderDao: OrderDao,
) : OrderService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_ORDER_UPDATED_WITH_PREMIUM = "Обновление общей суммы премии"
        const val STATUS_CODE_SUCCESS = 1101500200
        const val STATUS_CODE_SUCCESS_GET_ORDER_STATUS = 1201503200
        const val LOG_START_GET_ORDER_STATUS = "***** НАЧАЛО ***** метод получения статуса заявки для orderId: "
        const val LOG_END_GET_ORDER_STATUS = "***** НАЧАЛО ***** метод получения статуса заявки  stateId =  "
        const val LOG_START_ORDER_CREATION = "***** КОНЕЦ ***** создания заявки для TraceId: "
        const val LOG_END_ORDER_CREATION = "***** КОНЕЦ ***** создания заявки для TraceId: "
        const val LOG_ORDER_CREATION_SUCCESS = "Заказ успешно создан и сохранен в базу с orderCode: "
        const val LOG_SUB_ORDER_CREATION_SUCCESS = "Подзаказ успешно создан с subOrderId: "
        const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с не найден для TraceId: "
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
    override fun createOrder(requestWrapper: PaymentRequestWrapper): Response<DataOrder> {
        val traceId = getTraceId()
        logger.info(LOG_START_ORDER_CREATION + traceId)

        val savedOrder =
            formOrderFromRequest(requestWrapper)
                .apply {
                    val subOrdersFromRequest = formSubOrdersFromRequest(requestWrapper, this)
                    subOrders.addAll(subOrdersFromRequest)
                    premiumAmount = extractPremiumAmount(this.subOrders).setScale(2, RoundingMode.HALF_UP).toString()
                }.run(orderDao::save)

        logger.info("$LOG_END_ORDER_CREATION $traceId")
        return getSuccessResponse(
            traceId,
            STATUS_CODE_SUCCESS,
            formDataOrder(savedOrder.id),
        )
    }

    private fun formOrderFromRequest(requestWrapper: PaymentRequestWrapper): Order =
        Order(
            bank = BankEnum.fromValue(requestWrapper.bank),
            paymentEndDate = ZonedDateTime.parse(requestWrapper.paymentEndDate).toLocalDateTime(),
            urlToDecline = requestWrapper.urlToDecline,
            urlToReturn = requestWrapper.urlToReturn,
            recipientEmail = requestWrapper.recipientEmail,
        )

    private fun formSubOrdersFromRequest(
        requestWrapper: PaymentRequestWrapper,
        order: Order,
    ): List<SubOrder> =
        requestWrapper.payments
            .map(::formSubOrder)
            .onEach { it.order = order }

    private fun formSubOrder(paymentRequest: PaymentRequest): SubOrder =
        SubOrder(
            operationId = paymentRequest.operationId,
            externalSystemCode = ExternalSystemCodeEnum.valueOf(paymentRequest.externalSystemCode),
            docType = paymentRequest.docType,
            policyId = paymentRequest.policyId,
            policyNumber = paymentRequest.policyNumber,
            contractId = paymentRequest.contractId,
            typeInsurance = paymentRequest.typeInsurance,
            contractNumber = paymentRequest.contractNumber,
            insuranceProgram = paymentRequest.insuranceProgram,
            premiumAmount = paymentRequest.premiumAmount,
        )

    private fun extractPremiumAmount(subOrders: List<SubOrder>) = subOrders.sumOf { it.premiumAmount?.toBigDecimal() ?: BigDecimal.ZERO }

    private fun formDataOrder(orderId: UUID?) =
        DataOrder(
            orderId.toString(),
            "${apiConfigProperty.paymentUrl}$orderId",
        )

    override fun getOrderStatus(orderId: String): Response<DataGetOrderStatus> {
        val traceId = getTraceId()
        logger.info("$LOG_START_GET_ORDER_STATUS $orderId")
        val orderStatusId = orderDao.getOrderId(orderId).status
        logger.info("$LOG_END_GET_ORDER_STATUS $orderStatusId")
        return getSuccessResponse(traceId, STATUS_CODE_SUCCESS_GET_ORDER_STATUS, DataGetOrderStatus(orderStatusId.name))
    }
}
