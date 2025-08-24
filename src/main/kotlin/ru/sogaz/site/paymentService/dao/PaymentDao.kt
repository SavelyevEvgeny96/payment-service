package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.ERROR_UPDATE_PAYMENT_RECORD
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.PAYMENT_STATUS_REG

interface PaymentDao {
    fun getPayment(
        traceId: String,
        paymentId: Long,
    ): Payment?

    fun getPaymentFromBankId(bankId: String): Payment

    fun save(payment: Payment)

    fun findByPaymentBankId(paymentId: String): Payment
    fun paymentUpdate(
        paymentId: Long?,
        paymentPageUrl: String,
        qtcId: String,
    )
}
