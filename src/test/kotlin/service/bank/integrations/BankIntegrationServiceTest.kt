package service.bank.integrations

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import ru.sogaz.site.paymentService.dto.response.AkbOrderInfo
import ru.sogaz.site.paymentService.dto.response.AkbOrderResponse
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromSBPPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.IpsRuData
import ru.sogaz.site.paymentService.dto.response.Options
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.dto.response.QRCoreData
import ru.sogaz.site.paymentService.dto.response.QRImageData
import ru.sogaz.site.paymentService.dto.response.SBPData
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.payment.bank.integration.AKBankIntegrationServiceImpl
import ru.sogaz.site.paymentService.service.payment.bank.integration.GPBankIntegrationServiceImpl
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class BankIntegrationServiceTest {
    companion object {
        private const val TEST_GPB_TOKEN = "test-token"
        private const val TEST_PAYMENT_PAGE_URL = "payment-url"
        private const val TEST_GPB_SBP_QR_ID = "qr-id"
        private const val TEST_GPB_SBP_PAYLOAD = "gazprom-payload"
        private const val TEST_GPB_SBP_TRANSACTIONAL_ID = "transactional-id"
        private const val TEST_AKB_ORDER_ID = "123"
        private const val AKB_REDIRECT_URL = "afterPayRedirectUrl"
        private const val AKB_QRC_PAYLOAD = "akb-qrc-payload"
        private const val GPB_QR_CONTENT = "gpb-qr-content"
        private const val GPB_QR_MEDIA_TYPE = "image/png"

        private val TOKEN_RESPONSE = GazpromTokenResponse(TEST_GPB_TOKEN)
        private val GPB_CARD_PAYMENT_RESPONSE = GazpromCardPaymentResponse(TEST_GPB_TOKEN, Options(TEST_PAYMENT_PAGE_URL))
        private val GPB_SBP_PAYMENT_RESPONSE =
            GazpromSBPPaymentResponse(SBPData(TEST_GPB_SBP_QR_ID, TEST_GPB_SBP_PAYLOAD), TEST_GPB_SBP_TRANSACTIONAL_ID)
        private val AKB_ORDER_RESPONSE = AkbOrderResponse(AkbOrderInfo(TEST_AKB_ORDER_ID.toInt(), TEST_PAYMENT_PAGE_URL))
        private val PREPARE_PUSH_TRAN_RESPONSE =
            PreparePushTranResponse(
                mutableMapOf(
                    "ipsRu" to IpsRuData(AKB_QRC_PAYLOAD, AKB_REDIRECT_URL),
                ),
            )
        private val GPB_QR_IMAGE_RESPONSE = GPBQRImageResponse(QRCoreData(QRImageData(GPB_QR_CONTENT, GPB_QR_MEDIA_TYPE)))
    }

    @RelaxedMockK
    private lateinit var apiConfigProperties: ApiConfigProperties

    @MockK
    private lateinit var restTemplate: RestTemplate

    private lateinit var gpBankIntegrationService: GPBankIntegrationServiceImpl
    private lateinit var akBankIntegrationService: AKBankIntegrationServiceImpl

    @BeforeEach
    fun setUp() {
        mockGPBRestTemplate()
        mockAKBRestTemplate()

        gpBankIntegrationService = GPBankIntegrationServiceImpl(apiConfigProperties, restTemplate)
        akBankIntegrationService = AKBankIntegrationServiceImpl(apiConfigProperties, restTemplate)
    }

    @Test
    fun `successfully register CARD payment in GPB test`() {
        generateValidPayment(BankEnum.GPB, PaymentTypeEnum.CARD)
            .run(gpBankIntegrationService::registerPayment)
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(TEST_PAYMENT_PAGE_URL, Payment::paymentPageUrl)
            .returns(TEST_GPB_TOKEN, Payment::paymentBankId)
            .returns(null, Payment::qrcId)
    }

    @Test
    fun `successfully register SBP payment in GPB test`() {
        generateValidPayment(BankEnum.GPB, PaymentTypeEnum.SBP)
            .run(gpBankIntegrationService::registerPayment)
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(TEST_GPB_SBP_PAYLOAD, Payment::paymentPageUrl)
            .returns(TEST_GPB_SBP_TRANSACTIONAL_ID, Payment::paymentBankId)
            .returns(TEST_GPB_SBP_QR_ID, Payment::qrcId)
    }

    @Test
    fun `successfully register CARD payment in AKB test`() {
        generateValidPayment(BankEnum.AKB_RUS, PaymentTypeEnum.CARD)
            .run(akBankIntegrationService::registerPayment)
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(TEST_PAYMENT_PAGE_URL, Payment::paymentPageUrl)
            .returns(TEST_AKB_ORDER_ID, Payment::paymentBankId)
            .returns(null, Payment::qrcId)
    }

    @Test
    fun `successfully register SBP payment in AKB test`() {
        generateValidPayment(BankEnum.AKB_RUS, PaymentTypeEnum.SBP)
            .run(akBankIntegrationService::registerPayment)
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(AKB_QRC_PAYLOAD, Payment::paymentPageUrl)
            .returns(TEST_AKB_ORDER_ID, Payment::paymentBankId)
            .returns(null, Payment::qrcId)
    }

    @Test
    fun `successfully request for qr code image data`() {
        generateValidPayment(BankEnum.GPB, PaymentTypeEnum.SBP)
            .apply { qrcId = TEST_GPB_SBP_QR_ID }
            .run(gpBankIntegrationService::getQRCodeImageData)
            .run(::assertThat)
            .returns(GPB_QR_CONTENT) { it.data.image.content }
            .returns(GPB_QR_MEDIA_TYPE) { it.data.image.mediaType }
    }

    private fun generateValidPayment(
        bank: BankEnum,
        paymentType: PaymentTypeEnum,
    ) = Payment(
        bank = bank,
        order = generateOrder(subOrders = List(Random.nextInt(0, 10)) { generateSubOrder() }),
        type = paymentType,
    )

    private fun generateOrder(
        amount: Int = Random.nextInt(1, 10000),
        subOrders: List<SubOrder> = listOf(),
    ) = Order(
        premiumAmount = amount.toString(),
    ).apply { this.subOrders.addAll(subOrders) }

    private fun generateSubOrder() =
        SubOrder(
            externalSystemCode = ExternalSystemCodeEnum.LK,
        )

    private fun mockGPBRestTemplate() {
        every { restTemplate.postForObject<GazpromTokenResponse>(any(String::class)) }.returns(TOKEN_RESPONSE)
        every { restTemplate.postForObject<GazpromCardPaymentResponse>(any(String::class), any()) }.returns(GPB_CARD_PAYMENT_RESPONSE)
        every { restTemplate.postForObject<GazpromSBPPaymentResponse>(any(String::class), any()) }.returns(GPB_SBP_PAYMENT_RESPONSE)
        every { restTemplate.postForObject<GPBQRImageResponse>(any(String::class), any()) }.returns(GPB_QR_IMAGE_RESPONSE)
    }

    private fun mockAKBRestTemplate() {
        every { restTemplate.postForObject<AkbOrderResponse>(any(String::class), any()) }.returns(AKB_ORDER_RESPONSE)
        every { restTemplate.postForObject<PreparePushTranResponse>(any(String::class), any()) }.returns(PREPARE_PUSH_TRAN_RESPONSE)
        every { restTemplate.postForObject<Map<String, Any>>(any(String::class), any()) }.answers { mutableMapOf() }
    }
}
