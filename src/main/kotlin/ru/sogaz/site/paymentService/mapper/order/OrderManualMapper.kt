package ru.sogaz.site.paymentService.mapper.order

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.entity.Order

@Component
class OrderManualMapper(
    private val orderMapper: OrderMapper,
) {
    fun mapRequestToOrder(orderRequest: OrderRequest): Order {
        val order = orderMapper.fromRequestDto(orderRequest)

        val subOrders =
            orderRequest.orders
                .map(orderMapper::fromRequestDto)
                .onEach { it.order = order }

        order.subOrders.addAll(subOrders)
        return order
    }
}
