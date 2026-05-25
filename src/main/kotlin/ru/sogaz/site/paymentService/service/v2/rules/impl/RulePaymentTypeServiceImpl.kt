package ru.sogaz.site.paymentService.service.v2.rules.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.repository.v2.RulePaymentTypeRepository
import ru.sogaz.site.paymentService.service.v2.rules.RulePaymentTypeService

@Service
class RulePaymentTypeServiceImpl(
    private val rulePaymentTypeRepository: RulePaymentTypeRepository,
) : RulePaymentTypeService {
    private val logger = loggerFor(javaClass)

    override fun isOperationAvailable(
        operationType: OperationType,
        paymentType: PaymentType,
        bank: OperationBank?,
    ): Boolean {
        val resolvedBank = bank ?: OperationBank.ALL
        val bankRule = rulePaymentTypeRepository.findByBankAndPaymentTypeAndOperationType(resolvedBank, paymentType, operationType)

        if (bankRule != null) {
            logger.debug(
                "Правило найдено для банка [{}], paymentType [{}], operationType [{}], availability [{}]",
                resolvedBank,
                paymentType,
                operationType,
                bankRule.availability,
            )
            return bankRule.availability
        }

        val fallbackRule = if (resolvedBank != OperationBank.ALL) {
            rulePaymentTypeRepository.findByBankAndPaymentTypeAndOperationType(OperationBank.ALL, paymentType, operationType)
        } else {
            null
        }

        if (fallbackRule != null) {
            logger.warn(
                "Точное правило не найдено. Использован fallback ALL для paymentType [{}], operationType [{}], availability [{}]",
                paymentType,
                operationType,
                fallbackRule.availability,
            )
            return fallbackRule.availability
        }

        logger.warn(
            "Правило не найдено для банка [{}], paymentType [{}], operationType [{}]. Операция недоступна по умолчанию",
            resolvedBank,
            paymentType,
            operationType,
        )
        return false
    }
}
