package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.BankIntegrationService

@Component
class BankIntegrationFactoryService(
    private val apiConfigProperties: ApiConfigProperties,
    private val webConfigRestTemplate: WebConfigRestTemplate,
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
) {
    @Throws(BusinessException::class)
    fun getInstanceByBank(bankId: String?): BankIntegrationService = BankEnum.from(bankId).run(::getInstanceByBank)

    @Throws(BusinessException::class)
    fun getInstanceByBank(bankType: BankEnum?): BankIntegrationService =
        when (bankType) {
            BankEnum.GPB ->
                GPBankIntegrationServiceImpl(
                    apiConfigProperties,
                    webConfigRestTemplate.defaultRestTemplate(),
                    bankPaymentDetailsMapper,
                )
            BankEnum.AKB_RUS ->
                AKBankIntegrationServiceImpl(
                    apiConfigProperties,
                    webConfigRestTemplate.defaultRestTemplate(),
                    bankPaymentDetailsMapper,
                )
            else -> throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, getTraceId())
        }
}
