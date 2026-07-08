package ru.sogaz.site.paymentService.service.v2.bank.selection

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.paymentService.model.v2.entity.rules.PrioritizationRulesBanks
import ru.sogaz.site.paymentService.model.v2.entity.rules.RulesBanksProducts
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.enums.PaymentRequestBank
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.repository.v2.rules.PrioritizationRulesBanksRepository
import ru.sogaz.site.paymentService.repository.v2.rules.RulesBanksProductsRepository
import ru.sogaz.site.paymentService.service.v2.bank.selection.impl.OperationBankSelectionServiceImpl
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OperationBankSelectionServiceImplTest {
    @MockK
    private lateinit var prioritizationRulesBanksRepository: PrioritizationRulesBanksRepository

    @MockK
    private lateinit var rulesBanksProductsRepository: RulesBanksProductsRepository

    private val service by lazy {
        OperationBankSelectionServiceImpl(
            prioritizationRulesBanksRepository,
            rulesBanksProductsRepository,
        )
    }

    @Test
    fun `selectBank should use request bank when priority check disabled`() {
        every { prioritizationRulesBanksRepository.findFirstByOrderByUpdateDateDesc() } returns defaultRules()

        val result = service.selectBank(cardPayRequest(bank = PaymentRequestBank.ABR))

        assertThat(result).isEqualTo(OperationBank.ABR)
    }

    @Test
    fun `selectBank should use product rule when request bank missing`() {
        every { prioritizationRulesBanksRepository.findFirstByOrderByUpdateDateDesc() } returns defaultRules()
        every {
            rulesBanksProductsRepository.findFirstByInsuranceKindAndPaymentTypeAndActiveTrueOrderByUpdateDateDesc(
                TEST_INSURANCE_KIND,
                PaymentType.CARD,
            )
        } returns productRule(OperationBank.ABR)

        val result = service.selectBank(cardPayRequest())

        assertThat(result).isEqualTo(OperationBank.ABR)
    }

    @Test
    fun `selectBank should fallback to available reserve bank`() {
        every { prioritizationRulesBanksRepository.findFirstByOrderByUpdateDateDesc() } returns
            defaultRules(
                availableGpbCheck = false,
                availableAbrCheck = true,
            )

        val result = service.selectBank(cardPayRequest(bank = PaymentRequestBank.GPB))

        assertThat(result).isEqualTo(OperationBank.ABR)
    }

    private fun defaultRules(
        bankPriority: OperationBank = OperationBank.GPB,
        bankPriorityCheck: Boolean = false,
        bankReserve: OperationBank = OperationBank.ABR,
        partBankPriority: Int = 100,
        availableGpbCheck: Boolean = true,
        availableAbrCheck: Boolean = true,
    ) = PrioritizationRulesBanks(
        id = UUID.randomUUID(),
        bankPriority = bankPriority,
        bankPriorityCheck = bankPriorityCheck,
        bankReserve = bankReserve,
        partBankPriority = partBankPriority,
        availableGpbCheck = availableGpbCheck,
        availableAbrCheck = availableAbrCheck,
        createDate = null,
        updateDate = null,
    )

    private fun productRule(bank: OperationBank) =
        RulesBanksProducts(
            id = UUID.randomUUID(),
            insuranceKind = TEST_INSURANCE_KIND,
            program = null,
            bank = bank,
            paymentType = PaymentType.CARD,
            active = true,
            createDate = null,
            updateDate = null,
        )

    private fun cardPayRequest(bank: PaymentRequestBank? = null) =
        CardPayOperationRequest(
            orderId = UUID.randomUUID(),
            description = "description",
            amount = BigDecimal.TEN,
            depersonalization = false,
            payerIp = "127.0.0.1",
            insuranceKind = TEST_INSURANCE_KIND,
            bank = bank,
            params = RedirectParams(),
            saveCard = false,
        )

    private companion object {
        const val TEST_INSURANCE_KIND = "test-insurance-kind"
    }
}
