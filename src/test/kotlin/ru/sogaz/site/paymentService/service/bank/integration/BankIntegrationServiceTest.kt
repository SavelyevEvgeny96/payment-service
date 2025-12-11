package ru.sogaz.site.paymentService.service.bank.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClient
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpPaymentClient
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
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
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.mapper.payment.GPBPaymentRequestMapper
import ru.sogaz.site.paymentService.mapper.payment.GPBPaymentRequestMapperImpl
import ru.sogaz.site.paymentService.mapper.payment.RegisterCardMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.service.bank.integration.akb.AKBankIntegrationServiceImpl
import ru.sogaz.site.paymentService.service.bank.integration.gpb.GPBBankIntegrationGenerateDescriptionServiceImpl
import ru.sogaz.site.paymentService.service.bank.integration.gpb.GPBankIntegrationServiceImpl
import java.util.UUID
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class BankIntegrationServiceTest {
    companion object {
        private const val TEST_GPB_TOKEN = "test-token"
        private const val TEST_GPB_PAYMENT_PAGE_URL = "payment-url"
        private const val TEST_GPB_SBP_QR_ID = "qr-id"
        private const val TEST_GPB_SBP_PAYLOAD = "gazprom-payload"
        private const val TEST_GPB_SBP_TRANSACTIONAL_ID = "transactional-id"
        private const val TEST_AKB_ORDER_ID = "123"
        private const val TEST_AKB_PASSWORD = "akb-password"
        private const val TEST_AKB_HPP_URL = "payment-url?password=$TEST_AKB_PASSWORD"
        private const val AKB_REDIRECT_URL = "afterPayRedirectUrl"
        private const val AKB_QRC_PAYLOAD = "akb-qrc-payload"
        private const val GPB_QR_CONTENT = "gpb-qr-content"
        private const val GPB_QR_MEDIA_TYPE = "image/png"

        private val GPBTokenResponse = GazpromTokenResponse(TEST_GPB_TOKEN)
        private val GPBCardPaymentResponse =
            GazpromCardPaymentResponse(TEST_GPB_TOKEN, Options(TEST_GPB_PAYMENT_PAGE_URL))
        private val GPBSBPPaymentResponse =
            GazpromSBPPaymentResponse(SBPData(TEST_GPB_SBP_QR_ID, TEST_GPB_SBP_PAYLOAD), TEST_GPB_SBP_TRANSACTIONAL_ID)
        private val akbOrderInfo =
            AkbOrderInfo(id = TEST_AKB_ORDER_ID.toInt(), hppUrl = TEST_AKB_HPP_URL, password = TEST_AKB_PASSWORD)
        private val akbOrderResponse = AkbOrderResponse(akbOrderInfo)
        private val preparePushTranResponse =
            PreparePushTranResponse(mutableMapOf("ipsRu" to IpsRuData(AKB_QRC_PAYLOAD, AKB_REDIRECT_URL)))
        private val gpbqrImageResponse =
            GPBQRImageResponse(QRCoreData(QRImageData(GPB_QR_CONTENT, GPB_QR_MEDIA_TYPE)))
    }

    private val objectMapper: ObjectMapper =
        jacksonObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @RelaxedMockK
    private lateinit var apiConfigProperties: ApiConfigProperties

    @MockK
    private lateinit var restTemplate: RestTemplate

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var paymentDao: PaymentDao

    @MockK
    private lateinit var gpbCardPaymentClient: GpbCardPaymentClient

    @MockK
    private lateinit var gpbSbpPaymentClient: GpbSbpPaymentClient

    @MockK
    private lateinit var bankPaymentDetailsMapper: BankPaymentDetailsMapper

    @MockK
    private lateinit var registerCardMapper: RegisterCardMapper

    @RelaxedMockK
    lateinit var gpbBankIntegrationGenerateDescriptionServiceImpl: GPBBankIntegrationGenerateDescriptionServiceImpl

    @RelaxedMockK
    private lateinit var tokenService: TokenService

    private lateinit var gpBankIntegrationService: GPBankIntegrationServiceImpl
    private lateinit var akBankIntegrationService: AKBankIntegrationServiceImpl

    private lateinit var gPBPaymentRequestMapper: GPBPaymentRequestMapper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockGPBRestTemplate()
        mockAKBRestTemplate()

        // создаём маппер для GPB карточных запросов
        gPBPaymentRequestMapper =
            Mappers.getMapper(GPBPaymentRequestMapper::class.java) as GPBPaymentRequestMapperImpl

        gPBPaymentRequestMapper.tokenService = tokenService
        gPBPaymentRequestMapper.apiConfigProperties = apiConfigProperties
        gPBPaymentRequestMapper.gPBBankIntegrationGenerateDescriptionServiceImpl =
            gpbBankIntegrationGenerateDescriptionServiceImpl

        // создаём маппер статусов банка
        bankPaymentDetailsMapper =
            Mappers.getMapper(BankPaymentDetailsMapper::class.java)
        registerCardMapper = Mappers.getMapper(RegisterCardMapper::class.java)
        // сервис GPB
        gpBankIntegrationService =
            GPBankIntegrationServiceImpl(
                apiConfigProperties,
                gpbSbpPaymentClient,
                gpbCardPaymentClient,
                bankPaymentDetailsMapper,
                gPBPaymentRequestMapper,
                tokenService,
                objectMapper,
                registerCardMapper,
                orderDao,
                paymentDao,
            )

        // сервис АКБ
        akBankIntegrationService =
            AKBankIntegrationServiceImpl(
                apiConfigProperties,
                restTemplate,
                bankPaymentDetailsMapper,
            )
    }

    @Test
    fun `successfully register CARD payment in GPB test`() {
        generateValidPayment(BankEnum.GPB, PaymentTypeEnum.CARD)
            .run { gpBankIntegrationService.registerPayment(this, null) }
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(TEST_GPB_PAYMENT_PAGE_URL, Payment::paymentPageUrl)
            .returns(TEST_GPB_TOKEN, Payment::paymentBankId)
            .returns(null, Payment::qrcId)
    }

    @Test
    fun `successfully register SBP payment in GPB test`() {
        generateValidPayment(BankEnum.GPB, PaymentTypeEnum.SBP)
            .run { gpBankIntegrationService.registerPayment(this, null) }
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(TEST_GPB_SBP_PAYLOAD, Payment::paymentPageUrl)
            .returns(TEST_GPB_SBP_QR_ID, Payment::paymentBankId)
            .returns(TEST_GPB_SBP_QR_ID, Payment::qrcId)
    }

    @Test
    fun `successfully register CARD payment in AKB test`() {
        generateValidPayment(BankEnum.AKB_RUS, PaymentTypeEnum.CARD)
            .run { akBankIntegrationService.registerPayment(this, null) }
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(TEST_AKB_HPP_URL, Payment::paymentPageUrl)
            .returns(TEST_AKB_ORDER_ID, Payment::paymentBankId)
            .returns(TEST_AKB_PASSWORD, Payment::paymentPass)
            .returns(null, Payment::qrcId)
    }

    @Test
    fun `successfully register SBP payment in AKB test`() {
        generateValidPayment(BankEnum.AKB_RUS, PaymentTypeEnum.SBP)
            .run { akBankIntegrationService.registerPayment(this, null) }
            .run(::assertThat)
            .returns(PaymentStatusEnum.REG, Payment::state)
            .returns(AKB_QRC_PAYLOAD, Payment::paymentPageUrl)
            .returns(TEST_AKB_ORDER_ID, Payment::paymentBankId)
            .returns(TEST_AKB_PASSWORD, Payment::paymentPass)
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
        id = UUID.randomUUID(),
        bank = bank,
        order = generateOrder(),
        type = paymentType,
    )

    private fun generateOrder(
        amount: Int = Random.nextInt(1, 10000),
        subOrders: List<SubOrder> = listOf(),
    ) = Order(
        id = UUID.randomUUID(),
        premiumAmount = amount.toString(),
    ).apply { this.subOrders.addAll(subOrders) }

    private fun mockGPBRestTemplate() {
        every { gpbCardPaymentClient.getToken(any(String::class)) } returns GPBTokenResponse
        every { gpbCardPaymentClient.startPayment(any(), any(), any()) } returns GPBCardPaymentResponse
        every { gpbSbpPaymentClient.startPayment(any(), any()) } returns GPBSBPPaymentResponse
        every { gpbSbpPaymentClient.getQrImage(any()) } returns gpbqrImageResponse
    }

    private fun mockAKBRestTemplate() {
        every { restTemplate.postForObject<AkbOrderResponse>(any(String::class), any()) }.returns(akbOrderResponse)
        every { restTemplate.postForObject<PreparePushTranResponse>(any(String::class), any()) }.returns(
            preparePushTranResponse,
        )
        every { restTemplate.postForObject<Map<String, Any>>(any(String::class), any()) }.answers { mutableMapOf() }
    }
}
