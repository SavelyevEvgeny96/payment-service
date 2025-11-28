package ru.sogaz.site.paymentService.service.order

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.mapper.order.OrderManualMapper
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class OrderServiceImpl(
    private val orderDao: OrderDao,
    private val orderManualMapper: OrderManualMapper,
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

    private fun buildQueueStatusResultName(clientId: String?): String? {
        if (clientId.isNullOrBlank()) return null

        val normalizedClientId = clientId.replace(Regex("[^A-Za-zА-Яа-яЁё0-9]"), ".")

        return "payment.status.$normalizedClientId.created"
    }


    override fun makeOrderByRequest(orderRequest: OrderRequest): Order =
        orderManualMapper
            .mapRequestToOrder(orderRequest)
            .apply {
                queueStatusResultName = buildQueueStatusResultName(clientId)
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

    private fun Order.toDataGetOrderStatus() = DataGetOrderStatus(status.name)

    private inline fun <reified T> T.wrapToSuccessResponse(statusCode: Int): Response<T> =
        getSuccessResponse(getTraceId(), statusCode, this)
}
