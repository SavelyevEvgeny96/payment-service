package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.BankIntegrationService
import ru.sogaz.site.paymentService.service.payment.bank.integration.akb.AKBankIntegrationServiceImpl
import ru.sogaz.site.paymentService.service.payment.bank.integration.gpb.GPBBankIntegrationHelperServiceImpl
import ru.sogaz.site.paymentService.service.payment.bank.integration.gpb.GPBankIntegrationServiceImpl

@Component
class BankIntegrationFactoryService(
    private val apiConfigProperties: ApiConfigProperties,
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
    private val gpbBankIntegrationHelperServiceImpl: GPBBankIntegrationHelperServiceImpl,
) {
    @Throws(BusinessException::class)
    fun getInstanceByBank(bankId: String?): BankIntegrationService = BankEnum.from(bankId).run(::getInstanceByBank)

    @Throws(BusinessException::class)
    fun getInstanceByBank(bankType: BankEnum?): BankIntegrationService =
        when (bankType) {
            BankEnum.GPB ->
                GPBankIntegrationServiceImpl(
                    apiConfigProperties,
                    RestTemplate(),
                    gpbBankIntegrationHelperServiceImpl,
                )
            BankEnum.AKB_RUS ->
                AKBankIntegrationServiceImpl(
                    apiConfigProperties,
                    RestTemplate(),
                    bankPaymentDetailsMapper,
                )
            else -> throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, getTraceId())
        }
}
