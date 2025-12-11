package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v1.OrderCreateV1Api
import ru.sogaz.site.paymentService.api.doc.v1.OrderStatusV1Api
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.properties.ServiceStatuses
import ru.sogaz.site.paymentService.service.AuthorizationService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.siter.models.resonses.Response

@RestController
@Tag(name = "Order", description = "Управление заказами")
class OrderController(
    private val orderService: OrderService,
    private val authorizationService: AuthorizationService,
) : WrapResponseController(),
    OrderCreateV1Api,
    OrderStatusV1Api {
    override fun createOrder(
        requestWrapper: OrderRequest,
        authorization: String,
    ): ResponseEntity<Response<DataOrder>> {
        requestWrapper.clientId = authorizationService.checkPermissionByClientId(authorization)?.externalSystemCode
        return requestWrapper
            .run(orderService::createOrder)
            .wrapToSuccessResponse(ServiceStatuses.STATUS_CODE_SUCCESS)
            .wrapToOkResponseEntity()
    }

    override fun getOrderStatus(orderId: String): Response<DataGetOrderStatus> =
        orderId
            .run(orderService::getOrderStatus)
            .wrapToSuccessResponse(ServiceStatuses.STATUS_CODE_SUCCESS_GET_ORDER_STATUS)
}
