package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v1.PageInfoV1Api
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.properties.ServiceStatuses.Companion.SUCCESS_STATUS_CODE_PAY_INFO_PAGE
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

@RestController
@Tag(name = "PageInfo", description = "Получение информации о способах оплаты")
class PageInfoController(
    private val paymentService: PaymentService,
) : WrapResponseController(),
    PageInfoV1Api {
    override fun getInfoPage(
        @PathVariable orderId: UUID,
        payQueryParams: PayQueryParams,
    ): Response<DataOrderPaymentPageInfo> =
        paymentService
            .getOrderPaymentPageInfo(orderId, payQueryParams)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_PAY_INFO_PAGE)
}
