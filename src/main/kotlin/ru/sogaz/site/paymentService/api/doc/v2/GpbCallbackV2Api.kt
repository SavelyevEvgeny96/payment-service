package ru.sogaz.site.paymentService.api.doc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.dto.data.SbpGpbStateCallbackRequest
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCallbackResponse
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbGidCallbackRequest
import java.util.UUID

interface GpbCallbackV2Api {
    @GetMapping("v2/payment/gpb/state", produces = [MediaType.APPLICATION_XML_VALUE])
    fun stateGpbCallback(
        gpbCallback: GpbCardCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<GpbCallbackResponse>

    @Operation(summary = "Callback о состоянии операции оплаты картой в ГПБ ГИД")
    @ApiResponse(responseCode = "200", description = "Callback принят; тело ответа пустое")
    @PostMapping(
        "payment/gpb/gid/state",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun stateGpbGidCallback(
        @RequestHeader("X-Correlation-Id") correlationId: UUID,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = [
                    ExampleObject(name = "success", summary = "Успешная операция", value = SUCCESS_EXAMPLE),
                    ExampleObject(name = "interimSuccess", summary = "Промежуточный успех", value = INTERIM_SUCCESS_EXAMPLE),
                    ExampleObject(name = "failed", summary = "Ошибка операции", value = FAILED_EXAMPLE),
                    ExampleObject(name = "refund", summary = "Возврат", value = REFUND_EXAMPLE),
                ],
            )],
        )
        request: GpbGidCallbackRequest,
    ): ResponseEntity<Void>

    @PostMapping("v2/payment/sbp/gpb/state")
    fun stateSbpGpbCallback(
        @RequestBody request: SbpGpbStateCallbackRequest,
    )

    private companion object {
        const val SUCCESS_EXAMPLE = """{"pgaTrxId":"10001","state":"COMPLETED","result":{"status":"SUCCESS","rrn":"123456789012","approvalCode":"123456","merchantTrx":"550e8400-e29b-41d4-a716-446655440000","amount":1250.00,"params":{"source":"web"},"card":{"paymentSystem":"MIR","maskedPan":"220000******0000"}}}"""
        const val INTERIM_SUCCESS_EXAMPLE = """{"pgaTrxId":"10002","state":"PROCESSING","result":{"status":"INTERIM_SUCCESS","merchantTrx":"550e8400-e29b-41d4-a716-446655440000"}}"""
        const val FAILED_EXAMPLE = """{"pgaTrxId":"10003","state":"DECLINED","result":{"status":"FAILED","extendedCode":"DECLINED_BY_ISSUER","merchantTrx":"550e8400-e29b-41d4-a716-446655440000"}}"""
        const val REFUND_EXAMPLE = """{"pgaTrxId":"10004","state":"REFUNDED","result":{"status":"REFUND","rrn":"123456789012","merchantTrx":"550e8400-e29b-41d4-a716-446655440000","amount":1250.00}}"""
    }
}
