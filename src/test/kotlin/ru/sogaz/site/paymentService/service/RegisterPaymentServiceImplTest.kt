package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.paymentService.service.payment.RegisterPaymentServiceImpl
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class RegisterPaymentServiceImplTest {
    @MockK
    lateinit var paymentDao: PaymentDao

    @MockK
    lateinit var paymentOperationHistoryDao: PaymentOperationHistoryDao

    @MockK
    lateinit var bankIntegrationFactoryService: BankIntegrationFactoryService

    @MockK
    lateinit var bankIntegrationService: BankIntegrationService

    lateinit var registerPaymentService: RegisterPaymentServiceImpl

    @BeforeEach
    fun setUp() {
        registerPaymentService =
            RegisterPaymentServiceImpl(
                paymentDao,
                paymentOperationHistoryDao,
                bankIntegrationFactoryService,
            )
    }

    @Test
    fun `registerInBank should delegate call to BankIntegrationService`() {
        // given
        val payment = createTestPayment()
        val headers = GpbSbpHeadersParams("h1", "h2", "h3")
        val expectedPayment = createTestPayment()

        // mock factory to return bankIntegrationService
        every { bankIntegrationFactoryService.getInstanceByBank(payment.bank) } returns bankIntegrationService
        // mock bank integration service to return expected payment
        every { bankIntegrationService.registerPayment(payment, headers) } returns expectedPayment

        // when
        val result = registerPaymentService.registerInBank(payment, headers)

        // then
        assertThat(result).isEqualTo(expectedPayment)
        verify(exactly = 1) { bankIntegrationFactoryService.getInstanceByBank(payment.bank) }
        verify(exactly = 1) { bankIntegrationService.registerPayment(payment, headers) }
    }

    private fun createTestPayment(
        bank: BankEnum = BankEnum.GPB,
        type: PaymentTypeEnum = PaymentTypeEnum.CARD,
        depersonalization: Boolean = false,
    ): Payment {
        val order =
            Order().apply {
                id = UUID.randomUUID()
                premiumAmount = BigDecimal("1000.00").toString()
                saveCard = false
            }

        return Payment(
            id = UUID.randomUUID(),
            order = order,
            bank = bank,
            type = type,
            depersonalization = depersonalization,
            urlToReturn =
                UrlToReturn(
                    urlToReturnS = null,
                    urlToReturnF = null,
                ),
        )
    }
}
