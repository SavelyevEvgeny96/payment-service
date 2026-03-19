package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbCardAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardAccountManager

@Component
class GpbCardAccountManagerImpl(
    private val cardAccountProperties: GpbCardAccountProperties,
) : GpbCardAccountManager {
    override fun getByOperation(payOperationRequest: PayOperationRequest): GpbCardAccountData =
        getByDepersonalization(payOperationRequest.depersonalization)

    override fun getByOperation(payOperation: PayOperation): GpbCardAccountData = getByDepersonalization(payOperation.depersonalization)

    override fun getByDepersonalization(depersonalization: Boolean): GpbCardAccountData =
        cardAccountProperties.main.butIf(depersonalization) { cardAccountProperties.depersonalized }
}
