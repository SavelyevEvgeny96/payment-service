package ru.sogaz.site.paymentService.service.v2.pay.bank

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.entity.PrioritizationRulesBank
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.repository.v2.PrioritizationRulesBankRepository
import ru.sogaz.site.paymentService.repository.v2.RulesBanksProductRepository
import java.math.BigInteger
import java.security.MessageDigest

@Service
class BankResolverServiceImpl(
    private val prioritizationRulesBankRepository: PrioritizationRulesBankRepository,
    private val rulesBanksProductRepository: RulesBanksProductRepository,
) : BankResolverService {
    companion object {
        private const val CARD_PAYMENT_TYPE = "CARD"
        private const val PAYMENT_SYSTEM_NOT_AVAILABLE = "Платежная система недоступна"
    }

    override fun resolveBank(cardPayOperationRequest: CardPayOperationRequest): BankEnum {
        val priorities = prioritizationRulesBankRepository.findFirstByOrderByUpdateDateDesc() ?: return BankEnum.GPB

        val selectedBank =
            when (priorities.bankPriorityCheck) {
                true -> priorities.bankPriority.toBankOrDefault()
                false -> selectByRulesOrSplit(cardPayOperationRequest, priorities)
            }

        return checkAvailability(selectedBank, priorities)
    }

    private fun selectByRulesOrSplit(
        cardPayOperationRequest: CardPayOperationRequest,
        priorities: PrioritizationRulesBank,
    ): BankEnum =
        runCatching {
            cardPayOperationRequest.insuranceKind
                ?.takeIf(String::isNotBlank)
                ?.run { rulesBanksProductRepository.findFirstByInsuranceKindAndPaymentTypeAndActiveTrue(this, CARD_PAYMENT_TYPE) }
                ?.bank
                ?.toBankOrDefault()
                ?: splitByHash(cardPayOperationRequest, priorities)
        }.getOrElse {
            priorities.bankPriority.toBankOrDefault()
        }

    private fun splitByHash(
        cardPayOperationRequest: CardPayOperationRequest,
        priorities: PrioritizationRulesBank,
    ): BankEnum {
        val hashRemainder = sha256Mod100(cardPayOperationRequest.orderId.toString())
        return when (hashRemainder < priorities.partBankPriority) {
            true -> priorities.bankPriority.toBankOrDefault()
            false -> priorities.bankReserve.toBankOrDefault()
        }
    }

    private fun checkAvailability(
        selectedBank: BankEnum,
        priorities: PrioritizationRulesBank,
    ): BankEnum =
        when (selectedBank) {
            BankEnum.GPB -> when {
                priorities.availableGpbCheck -> BankEnum.GPB
                priorities.availableAbrCheck -> BankEnum.ABR
                else -> throw InnerException(getTraceId(), PAYMENT_SYSTEM_NOT_AVAILABLE)
            }

            BankEnum.ABR -> when {
                priorities.availableAbrCheck -> BankEnum.ABR
                priorities.availableGpbCheck -> BankEnum.GPB
                else -> throw InnerException(getTraceId(), PAYMENT_SYSTEM_NOT_AVAILABLE)
            }
        }

    private fun String.toBankOrDefault(): BankEnum =
        when (trim().uppercase()) {
            "GPB" -> BankEnum.GPB
            "ABR", "AKB_RUS" -> BankEnum.ABR
            else -> BankEnum.GPB
        }

    private fun sha256Mod100(value: String): Int {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return BigInteger(1, digest).mod(BigInteger.valueOf(100)).toInt()
    }
}
