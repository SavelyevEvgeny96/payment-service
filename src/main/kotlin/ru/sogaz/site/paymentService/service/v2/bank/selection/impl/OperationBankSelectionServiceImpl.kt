package ru.sogaz.site.paymentService.service.v2.bank.selection.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.model.v2.entity.rules.PrioritizationRulesBanks
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.repository.v2.rules.PrioritizationRulesBanksRepository
import ru.sogaz.site.paymentService.repository.v2.rules.RulesBanksProductsRepository
import ru.sogaz.site.paymentService.service.v2.bank.selection.OperationBankSelectionService
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

@Service
class OperationBankSelectionServiceImpl(
    private val prioritizationRulesBanksRepository: PrioritizationRulesBanksRepository,
    private val rulesBanksProductsRepository: RulesBanksProductsRepository,
) : OperationBankSelectionService {
    private val logger = loggerFor(javaClass)

    companion object {
        private const val PAYMENT_SYSTEM_UNAVAILABLE_ERROR =
            "Ошибка совершения платежа. Платежная система недоступна"
        private const val PRIORITIZATION_RULE_NOT_FOUND_ERROR = "Правило приоритизации банков не найдено"
        private const val INVALID_PART_BANK_PRIORITY_ERROR = "Некорректный процент распределения платежей по банкам"
        private const val MIN_PERCENT = 0
        private const val MAX_PERCENT = 100
        private const val PERCENT_BUCKET_DIVISOR = 100
        private const val HASH_ALGORITHM = "SHA-256"
    }

    override fun selectBank(payOperationRequest: PayOperationRequest): OperationBank {
        val rules = prioritizationRulesBanksRepository.findFirstByOrderByUpdateDateDesc()
            ?: throw InnerException(getTraceId(), PRIORITIZATION_RULE_NOT_FOUND_ERROR)
        rules.validatePartBankPriority()

        val selectedBank = payOperationRequest.selectByRules(rules)
        val availableBank = rules.resolveAvailableBank(selectedBank)
        logger.debug(
            "Для операции выбран банк [{}]. operationType [{}], paymentType [{}], orderId [{}]",
            availableBank,
            payOperationRequest.operationType,
            payOperationRequest.paymentType,
            payOperationRequest.orderId,
        )
        return availableBank
    }

    private fun PayOperationRequest.selectByRules(rules: PrioritizationRulesBanks): OperationBank {
        if (rules.bankPriorityCheck) {
            return rules.bankPriority
        }

        bank?.let { return it.toOperationBank() }

        val productRuleBank = insuranceKind
            ?.takeIf(String::isNotBlank)
            ?.let { rulesBanksProductsRepository.findFirstByInsuranceKindAndPaymentTypeAndActiveTrueOrderByUpdateDateDesc(it, paymentType) }
            ?.bank

        if (productRuleBank != null) {
            logger.debug(
                "Найдено правило распределения банка для insuranceKind [{}], paymentType [{}], bank [{}]",
                insuranceKind,
                paymentType,
                productRuleBank,
            )
            return productRuleBank
        }

        return runCatching { selectByPercent(rules) }
            .onFailure { logger.warn("Ошибка процентного распределения банка. Будет выбран приоритетный банк", it) }
            .getOrDefault(rules.bankPriority)
    }

    private fun PayOperationRequest.selectByPercent(rules: PrioritizationRulesBanks): OperationBank {
        val bucket = orderId.toStablePercentBucket()
        return when {
            bucket < rules.partBankPriority -> rules.bankPriority
            else -> rules.bankReserve
        }
    }

    private fun UUID?.toStablePercentBucket(): Int {
        val source = requireNotNull(this).toString()
        val digest = MessageDigest.getInstance(HASH_ALGORITHM).digest(source.toByteArray())
        return BigInteger(1, digest).mod(BigInteger.valueOf(PERCENT_BUCKET_DIVISOR.toLong())).toInt()
    }

    private fun PrioritizationRulesBanks.resolveAvailableBank(selectedBank: OperationBank): OperationBank =
        when (selectedBank) {
            OperationBank.GPB -> resolveGpbAvailability()
            OperationBank.ABR -> resolveAbrAvailability()
            OperationBank.ALL -> throw InnerException(getTraceId(), PAYMENT_SYSTEM_UNAVAILABLE_ERROR)
        }

    private fun PrioritizationRulesBanks.resolveGpbAvailability(): OperationBank =
        when {
            availableGpbCheck -> OperationBank.GPB
            availableAbrCheck -> OperationBank.ABR
            else -> throw InnerException(getTraceId(), PAYMENT_SYSTEM_UNAVAILABLE_ERROR)
        }

    private fun PrioritizationRulesBanks.resolveAbrAvailability(): OperationBank =
        when {
            availableAbrCheck -> OperationBank.ABR
            availableGpbCheck -> OperationBank.GPB
            else -> throw InnerException(getTraceId(), PAYMENT_SYSTEM_UNAVAILABLE_ERROR)
        }

    private fun PrioritizationRulesBanks.validatePartBankPriority() {
        if (partBankPriority in MIN_PERCENT..MAX_PERCENT) return
        throw InnerException(getTraceId(), INVALID_PART_BANK_PRIORITY_ERROR)
    }
}
