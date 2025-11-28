package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.paymentService.service.payment.RegisterPaymentServiceImpl

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
        val payment = Payment(bank = BankEnum.GPB, type = PaymentTypeEnum.CARD)
        val headers = GpbSbpHeadersParams("h1","h2","h3")
        val recurrent = true
        val expectedPayment = Payment(bank = BankEnum.GPB, type = PaymentTypeEnum.CARD)

        // mock factory to return bankIntegrationService
        every { bankIntegrationFactoryService.getInstanceByBank(payment.bank) } returns bankIntegrationService
        // mock bank integration service to return expected payment
        every { bankIntegrationService.registerPayment(payment, headers, recurrent) } returns expectedPayment

        // when
        val result = registerPaymentService.registerInBank(payment, headers, recurrent)

        // then
        assertThat(result).isEqualTo(expectedPayment)
        verify(exactly = 1) { bankIntegrationFactoryService.getInstanceByBank(payment.bank) }
        verify(exactly = 1) { bankIntegrationService.registerPayment(payment, headers, recurrent) }
    }
}
