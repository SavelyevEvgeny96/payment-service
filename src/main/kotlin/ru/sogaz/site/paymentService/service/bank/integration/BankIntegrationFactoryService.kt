package ru.sogaz.site.paymentService.service.bank.integration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.service.BankIntegrationService

@Component
class BankIntegrationFactoryService(
    @param:Qualifier("GPBankIntegrationServiceImpl")
    private val gpBankIntegrationService: BankIntegrationService,
    @param:Qualifier("AKBankIntegrationServiceImpl")
    private val akBankIntegrationService: BankIntegrationService,
) {
    @Throws(BusinessException::class)
    fun getInstanceByBank(bankId: String?): BankIntegrationService = BankEnum.from(bankId).run(::getInstanceByBank)

    @Throws(BusinessException::class)
    fun getInstanceByBank(bankType: BankEnum?): BankIntegrationService =
        when (bankType) {
            BankEnum.GPB -> gpBankIntegrationService
            BankEnum.AKB_RUS -> akBankIntegrationService
            else -> throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, getTraceId())
        }
}
