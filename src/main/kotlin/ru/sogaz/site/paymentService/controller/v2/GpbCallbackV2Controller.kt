package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.GpbCallbackV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCallbackResponse
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.exception.InvalidSignatureException
import ru.sogaz.site.paymentService.model.v2.exception.OperationNotFoundException
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.v2.status.OperationCallbackService

@RestController
@Tag(name = "Callback v2", description = "Прием callback-ов от банков")
class GpbCallbackV2Controller(
    private val operationCallbackService: OperationCallbackService,
    private val signatureVerifier: SignatureVerifier,
) : WrapResponseController(),
    GpbCallbackV2Api {
    companion object {
        private const val INTERNAL_SERVER_ERROR = "Internal server error"
        private const val INVALID_SIGNATURE = "Invalid signature"
        private const val NOT_FOUND = "Not Found"
    }

    private val logger = loggerFor(javaClass)

    override fun stateGpbCallback(
        gpbCallback: GpbCardCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<GpbCallbackResponse> =
        try {
            if (!signatureVerifier.verifySignature(gpbCallback, httpServletRequest)) {
                throw InvalidSignatureException(gpbCallback.merchant_trx, gpbCallback.trx_id)
            }
            operationCallbackService.updateByGpbCardCallback(gpbCallback)
            GpbCallbackResponse()
        } catch (ex: Exception) {
            logger.error(ex.message)
            when (ex) {
                is OperationNotFoundException -> GpbCallbackResponse(NOT_FOUND)
                is InvalidSignatureException -> GpbCallbackResponse(INVALID_SIGNATURE)
                else -> GpbCallbackResponse(INTERNAL_SERVER_ERROR)
            }
        }.wrapToOkResponseEntity()

    override fun stateSbpGpbCallback(qrcId: String) {
        operationCallbackService.updateByPaymentBankId(qrcId)
    }
}
