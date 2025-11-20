package ru.sogaz.site.paymentService.service.order

import org.springframework.stereotype.Service
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.dto.request.SubOrderRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneId
import java.util.UUID

@Service
class OrderServiceImpl(
    private val apiConfigProperty: ApiConfigProperties,
    private val orderDao: OrderDao,
) : OrderService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val STATUS_CODE_SUCCESS = 1101500200
        const val STATUS_CODE_SUCCESS_GET_ORDER_STATUS = 1201503200
        const val LOG_START_GET_ORDER_STATUS = "***** НАЧАЛО ***** метод получения статуса заявки для orderId: "
        const val LOG_END_GET_ORDER_STATUS = "***** КОНЕЦ ***** метод получения статуса заявки  stateId =  "
        const val LOG_START_ORDER_CREATION = "***** НАЧАЛО ***** создания заявки для TraceId: "
        const val LOG_END_ORDER_CREATION = "***** КОНЕЦ ***** создания заявки для TraceId: "
        const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с не найден для TraceId: "
        const val ERROR_CLIENT_SYSTEM_NOT_FOUND = "Система клиента не найдена"
    }

    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param requestWrapper Данные о заказе(содержит внутри лист PaymentRequest)
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект Payment, содержащий информацию о платежном запросе
     */
    override fun createOrder(requestWrapper: OrderRequest): Response<DataOrder> {
        logger.info(LOG_START_ORDER_CREATION)
        val savedOrder = saveEntityFromRequest(requestWrapper)
        logger.info(LOG_END_ORDER_CREATION)
        return getSuccessResponse(
            getTraceId(),
            STATUS_CODE_SUCCESS,
            formDataOrder(savedOrder.id),
        )
    }

    override fun saveEntityFromRequest(requestWrapper: OrderRequest): Order =
        formOrderFromRequest(requestWrapper)
            .apply {
                val subOrdersFromRequest = formSubOrdersFromRequest(requestWrapper, this)
                subOrders.addAll(subOrdersFromRequest)
                premiumAmount = extractPremiumAmount(this.subOrders).setScale(2, RoundingMode.HALF_UP).toString()
            }.run(orderDao::save)

    private fun formOrderFromRequest(requestWrapper: OrderRequest): Order =
        Order(
            paymentEndDate = requestWrapper.orderEndDate?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
            urlToDecline = requestWrapper.urlToDecline,
            urlToReturn = requestWrapper.urlToReturn,
            recipientEmail = requestWrapper.recipientEmail,
            subscriptionId = requestWrapper.subscriptionId,
            clientId = requestWrapper.clientId,
            recipientPhone = requestWrapper.recipientPhone,
            saveCard = requestWrapper.saveCard,
            policyholder = requestWrapper.policyholder,
            unifiedId = requestWrapper.unifiedId,
            orderIdRecurrent = requestWrapper.orderIdRecurrent,
            bank = requestWrapper.bank,
            keyCard = requestWrapper.keyCard,

        )

    private fun formSubOrdersFromRequest(
        requestWrapper: OrderRequest,
        order: Order,
    ): List<SubOrder> =
        requestWrapper.orders
            .map(::formSubOrder)
            .onEach { it.order = order }

    private fun formSubOrder(subOrderRequest: SubOrderRequest): SubOrder =
        SubOrder(
            docType = subOrderRequest.docType,
            policyId = subOrderRequest.policyId,
            policyNumber = subOrderRequest.policyNumber,
            contractId = subOrderRequest.contractId,
            typeInsurance = subOrderRequest.typeInsurance,
            mainContractCheck = subOrderRequest.mainContractCheck,
            contractNumber = subOrderRequest.contractNumber,
            insuranceProgram = subOrderRequest.insuranceProgram,
            premiumAmount = subOrderRequest.premiumAmount.toString(),
            policyDate = subOrderRequest.policyDate,
            contractDate = subOrderRequest.contractDate,
            managerEmail = subOrderRequest.managerEmail,
            channel = subOrderRequest.channel,
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
