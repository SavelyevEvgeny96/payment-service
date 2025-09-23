package ru.sogaz.site.paymentService.service.payment.bank.integration

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.SslClientProperties
import ru.sogaz.site.paymentService.service.BankIntegrationService

class BankIntegrationFactoryService(
    private val apiConfigProperties: ApiConfigProperties,
    private val webConfigRestTemplate: WebConfigRestTemplate,
    private val props: SslClientProperties,
) {
    @Throws(BusinessException::class)
    fun getInstanceByBank(bankId: String?): BankIntegrationService = BankEnum.fromValue(bankId).run(::getInstanceByBank)

    @Throws(BusinessException::class)
    fun getInstanceByBank(bankType: BankEnum?): BankIntegrationService =
        when (bankType) {
            BankEnum.GPB -> GPBankIntegrationServiceImpl(apiConfigProperties, webConfigRestTemplate.defaultRestTemplate())
            BankEnum.AKB_RUS -> AKBankIntegrationServiceImpl(apiConfigProperties, webConfigRestTemplate.defaultRestTemplate())
            else -> throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, getTraceId())
        }
}
