package ru.sogaz.site.paymentService.service.order

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.mapper.order.OrderManualMapper
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.paymentService.service.order.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_PATTERN
import ru.sogaz.site.paymentService.service.order.QueueStatusResultNameNormalizeServiceImpl.Companion.PAYMENT_STATUS_PATTERN
import ru.sogaz.site.paymentService.service.payment.RegisterPaymentServiceImpl
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class OrderServiceImpl(
    private val orderDao: OrderDao,
    private val orderManualMapper: OrderManualMapper,
    private val queueStatusResultNameNormalizeService: QueueStatusResultNameNormalizeService,
) : OrderService {
    @Value("\${api.payment.paymentUrl}")
    lateinit var payBasePath: String

    /**
     * Метод для создания платежа.
     * @param orderRequest Данные о заказе(содержит внутри лист subOrderRequest)
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект RDataOrder, содержащий информацию о платежном запросе
     */
    override fun createOrder(orderRequest: OrderRequest): DataOrder =
        orderRequest
            .run(::makeOrderByRequest)
            .run(orderDao::save)
            .toDataOrder()

    override fun makeOrderByRequest(orderRequest: OrderRequest): Order =
        orderManualMapper
            .mapRequestToOrder(orderRequest)
            .apply {
                queueStatusResultName =
                    queueStatusResultNameNormalizeService.buildQueueStatusResultName(PAYMENT_STATUS_PATTERN, clientId)
                premiumAmount =
                    extractPremiumAmount(subOrders)
                        .setScale(2, RoundingMode.HALF_UP)
                        .toString()
            }

    private fun extractPremiumAmount(subOrders: List<SubOrder>) = subOrders.sumOf { it.premiumAmount?.toBigDecimal() ?: BigDecimal.ZERO }

    private fun Order.toDataOrder() = DataOrder(id!!, "$payBasePath$id")

    override fun getOrderStatus(orderId: String): DataGetOrderStatus =
        orderId
            .run(orderDao::getOrderId)
            .toDataGetOrderStatus()

    override fun createRegestryOrder(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): Order =
        Order(
            paymentEndDate = LocalDateTime.now().plusHours(4),
            premiumAmount = "1",
            unifiedId = unifiedId,
            urlToReturn = payQueryParams.urlToReturnS.toString(),
            urlToDecline = payQueryParams.urlToReturnF.toString(),
            saveCard = true,
            regCard = true,
            skipSendingReceipt = true,
            skipSendingQueue = false,
            queueStatusResultName =
                queueStatusResultNameNormalizeService.buildQueueStatusResultName(
                    ORDER_STATUS_PATTERN,
                    clientId,
                ),
            bank = BankEnum.GPB,
            clientId = clientId,
        ).run(orderDao::save)

    override fun cancelOrder(order: Order) {
        order.status = OrderStatus.CANCELED
        orderDao.save(order)
    }

    override fun cancelOrderIfPaymentFail(order: Order) {
        order.id?.let {
            val orderEntity = orderDao.getOrderId(it)
            if (orderEntity.payments.any { it.state == PaymentStatusEnum.FAIL }) {
                orderEntity.status = OrderStatus.CANCELED
                orderDao.save(orderEntity)
                throw BusinessException(CustomPaymentErrors.CODE_ERROR_REGISTRY_CARD_NOT_AVAILABLE_INFO, getTraceId())
            }
        } ?: run {
            InnerException(
                getTraceId(),
                RegisterPaymentServiceImpl.ERROR_PAYMENT_PROCESSING,
            )
        }
    }

    private fun Order.toDataGetOrderStatus() = DataGetOrderStatus(status.name)

    private inline fun <reified T> T.wrapToSuccessResponse(statusCode: Int): Response<T> =
        getSuccessResponse(getTraceId(), statusCode, this)
}
