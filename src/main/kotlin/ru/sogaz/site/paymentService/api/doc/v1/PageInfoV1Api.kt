package ru.sogaz.site.paymentService.api.doc.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface PageInfoV1Api {
    @Operation(
        summary = "Информация о способах оплаты заказа",
        description = "Возвращает ссылку для оплаты картой и, если возможно оплатить по СБП, QR-code для оплаты по СБП",
    )
    @Parameters(
        Parameter(name = "orderId", description = "UUID заказа для оплаты", required = true),
        Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты", example = "http://www.sogaz.ru"),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты"),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты"),
        Parameter(name = "depersonalization", description = "Флаг необходимости анонимизированной оплаты", example = "true"),
    )
    @GetMapping("/payment/pageinfo/{orderId}")
    fun getInfoPage(
        @PathVariable orderId: UUID,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
    ): Response<DataOrderPaymentPageInfo>
}
