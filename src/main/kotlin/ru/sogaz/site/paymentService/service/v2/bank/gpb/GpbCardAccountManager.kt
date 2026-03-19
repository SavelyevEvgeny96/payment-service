package ru.sogaz.site.paymentService.service.v2.bank.gpb

import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest

interface GpbCardAccountManager {
    fun getByOperation(payOperationRequest: PayOperationRequest): GpbCardAccountData

    fun getByOperation(payOperation: PayOperation): GpbCardAccountData

    fun getByDepersonalization(depersonalization: Boolean): GpbCardAccountData
}
