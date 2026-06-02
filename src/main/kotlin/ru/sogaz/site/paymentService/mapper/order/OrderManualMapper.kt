package ru.sogaz.site.paymentService.mapper.order

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.entity.Order
import java.math.RoundingMode

@Component
class OrderManualMapper(
    private val orderMapper: OrderMapper,
) {
    fun mapRequestToOrder(orderRequest: OrderRequest): Order {
        val order = orderMapper.fromRequestDto(orderRequest)
        val subOrders =
            orderRequest.orders
                .map(orderMapper::fromRequestDto)
                .onEach { subOrder ->
                    subOrder.order = order
                    subOrder.premiumAmount =
                        subOrder.premiumAmount
                            ?.toBigDecimal()
                            ?.setScale(2, RoundingMode.HALF_UP)
                            ?.toPlainString()
                }

        order.subOrders.addAll(subOrders)
        return order
    }
}
