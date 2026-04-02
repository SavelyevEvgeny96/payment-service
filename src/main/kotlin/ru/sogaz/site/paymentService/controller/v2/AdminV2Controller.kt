package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v2.AdminV2Api
import ru.sogaz.site.paymentService.controller.WrapResponseController
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbSbpAdminAutoPayRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbSbpAutoPayHeaders
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GpbSbpAdminAutoPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.gpb.impl.GpbSbpAutoPayIntegrationImpl
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.gpbOperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave

@RestController
@Profile(value = ["test", "local", "stage"])
@Tag(name = "Admin v2", description = "Административный контроллер недоступный на проде")
class AdminV2Controller(
    private val operationService: OperationService,
    private val gpbSbpIntegration: GpbSbpAutoPayIntegrationImpl,
    private val gpbSbpAdminAutoPayRequestMapper: GpbSbpAdminAutoPayRequestMapper,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
) : WrapResponseController(),
    AdminV2Api {
    override fun sbpAutoPay(
        paymentDelay: String?,
        processPayments: String?,
        paymentStatus: String?,
        sbpPayOperationRequest: SbpPayOperationRequest,
    ): BankPaymentPageData {
        val headers = GpbSbpAutoPayHeaders(paymentDelay, processPayments, paymentStatus)
        val adminRequest = gpbSbpAdminAutoPayRequestMapper.toAdminRequest(sbpPayOperationRequest, headers)
        return adminRequest
            .operationCommand()
            .run(operationService::runOperation)
    }

    private fun GpbSbpAdminAutoPayOperationRequest.operationCommand() =
        gpbOperationCommand(
            stepWithSave(
                action = gpbSbpIntegration::sbpAdminAutoPay,
                resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
            ),
        )
}
