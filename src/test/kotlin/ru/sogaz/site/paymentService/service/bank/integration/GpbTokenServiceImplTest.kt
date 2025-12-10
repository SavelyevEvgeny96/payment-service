package ru.sogaz.site.paymentService.service.bank.integration
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClient
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.service.bank.integration.gpb.BankIntegrationTokenService
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class GpbTokenServiceImplTest {
    @MockK
    lateinit var gpbCardPaymentClient: GpbCardPaymentClient

    @MockK
    lateinit var apiConfigProperties: ApiConfigProperties

    @MockK
    lateinit var paymentDao: PaymentDao

    private lateinit var service: TokenService

    @BeforeEach
    fun setup() {
        service =
            BankIntegrationTokenService(
                gpbCardPaymentClient = gpbCardPaymentClient,
                apiConfigProperties = apiConfigProperties,
                paymentDao = paymentDao,
            )

        every { apiConfigProperties.mainPortalId } returns "MAIN_PORTAL"
        every { apiConfigProperties.depersonalizedPortalId } returns "DEP_PORTAL"

        every { apiConfigProperties.mainMerchantId } returns "MAIN_MERCHANT"
        every { apiConfigProperties.depersonalizedMerchantId } returns "DEP_MERCHANT"
    }

    // -------------------------------------------------------------
    // exchangeForToken
    // -------------------------------------------------------------

    @Test
    fun `exchangeForToken - success`() {
        every { gpbCardPaymentClient.getToken("MAIN_PORTAL") } returns GazpromTokenResponse("TOKEN_123")

        val token = service.exchangeForToken(false)

        assertThat(token).isEqualTo("TOKEN_123")
        verify { gpbCardPaymentClient.getToken("MAIN_PORTAL") }
    }

    // -------------------------------------------------------------
    // saveToken
    // -------------------------------------------------------------

    @Test
    fun `saveToken - saves token inside payment and calls dao`() {
        val payment = createTestPayment()

        every { gpbCardPaymentClient.getToken("MAIN_PORTAL") } returns GazpromTokenResponse("TOKEN_777")
        every { paymentDao.save(any()) } returns payment

        val result = service.saveToken(payment)

        assertThat(result).isEqualTo("TOKEN_777")
        assertThat(payment.paymentBankId).isEqualTo("TOKEN_777")

        verify { paymentDao.save(payment) }
    }

    // -------------------------------------------------------------
    // takePortalId
    // -------------------------------------------------------------

    @Test
    fun `takePortalId - regular`() {
        val result = service.takePortalId(false)
        assertThat(result).isEqualTo("MAIN_PORTAL")
    }

    @Test
    fun `takePortalId - depersonalized`() {
        val result = service.takePortalId(true)
        assertThat(result).isEqualTo("DEP_PORTAL")
    }

    // -------------------------------------------------------------
    // takeMerchantId
    // -------------------------------------------------------------

    @Test
    fun `takeMerchantId - regular`() {
        val result = service.takeMerchantId(false)
        assertThat(result).isEqualTo("MAIN_MERCHANT")
    }

    @Test
    fun `takeMerchantId - depersonalized`() {
        val result = service.takeMerchantId(true)
        assertThat(result).isEqualTo("DEP_MERCHANT")
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
