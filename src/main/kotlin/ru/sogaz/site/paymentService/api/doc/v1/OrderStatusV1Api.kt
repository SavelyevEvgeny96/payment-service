package ru.sogaz.site.paymentService.api.doc.v1

import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.siter.models.resonses.Response

interface OrderStatusV1Api {
    @GetMapping("payment/order/status/{orderId}")
    @ApiResponse(responseCode = "200", description = "Успешное получение статуса")
    fun getOrderStatus(
        @PathVariable orderId: String,
    ): Response<DataGetOrderStatus>
}
