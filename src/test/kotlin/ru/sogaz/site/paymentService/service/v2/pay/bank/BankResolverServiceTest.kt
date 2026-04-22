package ru.sogaz.site.paymentService.service.v2.pay.bank

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.entity.PrioritizationRulesBank
import ru.sogaz.site.paymentService.model.v2.entity.RulesBanksProduct
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.repository.v2.PrioritizationRulesBankRepository
import ru.sogaz.site.paymentService.repository.v2.RulesBanksProductRepository
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class BankResolverServiceTest {
    @MockK
    private lateinit var prioritizationRulesBankRepository: PrioritizationRulesBankRepository

    @MockK
    private lateinit var rulesBanksProductRepository: RulesBanksProductRepository

    private lateinit var bankResolverService: BankResolverService

    @BeforeEach
    fun beforeEach() {
        bankResolverService = BankResolverServiceImpl(prioritizationRulesBankRepository, rulesBanksProductRepository)
    }

    @Test
    fun `should return GPB when no settings found`() {
        every { prioritizationRulesBankRepository.findFirstByOrderByUpdateDateDesc() } returns null

        val result = bankResolverService.resolveBank(cardRequest())

        assertThat(result).isEqualTo(BankEnum.GPB)
    }

    @Test
    fun `should use priority bank when priority check enabled`() {
        every { prioritizationRulesBankRepository.findFirstByOrderByUpdateDateDesc() } returns priorities(bankPriorityCheck = true, bankPriority = "ABR")

        val result = bankResolverService.resolveBank(cardRequest())

        assertThat(result).isEqualTo(BankEnum.ABR)
    }

    @Test
    fun `should use bank from product rule when priority check disabled`() {
        every { prioritizationRulesBankRepository.findFirstByOrderByUpdateDateDesc() } returns priorities(bankPriorityCheck = false)
        every {
            rulesBanksProductRepository.findFirstByInsuranceKindAndPaymentTypeAndActiveTrue("OSAGO", "CARD")
        } returns rulesBankProduct(bank = "ABR")

        val result = bankResolverService.resolveBank(cardRequest(insuranceKind = "OSAGO"))

        assertThat(result).isEqualTo(BankEnum.ABR)
    }

    private fun cardRequest(insuranceKind: String? = null) =
        CardPayOperationRequest(
            orderId = UUID.randomUUID(),
            description = "test",
            amount = BigDecimal.TEN,
            depersonalization = false,
            payerIp = null,
            bank = null,
            insuranceKind = insuranceKind,
            saveCard = false,
        )

    private fun priorities(
        bankPriorityCheck: Boolean,
        bankPriority: String = "GPB",
    ) =
        PrioritizationRulesBank(
            id = UUID.randomUUID(),
            bankPriority = bankPriority,
            bankPriorityCheck = bankPriorityCheck,
            bankReserve = "ABR",
            partBankPriority = 50,
            availableGpbCheck = true,
            availableAbrCheck = true,
            createDate = null,
            updateDate = null,
        )

    private fun rulesBankProduct(bank: String) =
        RulesBanksProduct(
            id = UUID.randomUUID(),
            insuranceKind = "OSAGO",
            program = "PROGRAM",
            bank = bank,
            paymentType = "CARD",
            active = true,
            createDate = null,
            updateDate = null,
        )
}
