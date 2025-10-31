package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.dto.data.SubOrderInfo
import ru.sogaz.site.paymentService.dto.request.PageInfoRequestParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.MediaTypeValue
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.service.payment.InfoPageServiceImpl
import ru.sogaz.site.paymentService.service.payment.PaymentServiceImpl
import java.net.URI
import java.util.UUID

@ExtendWith(MockKExtension::class)
class InfoPageServiceTest {
    companion object {
        private const val QR_CONTENT = "QR Content"
        private const val BASE_PAYMENT_CARD_PAY_PATH = "http://payment-card-test.ru/"
        private const val BASE_PAYMENT_SBP_PAY_PATH = "http://payment-sbp-test.ru/"

        private const val ORDER_TEST_AMOUNT = "10000"

        private const val FIRST_SUB_ORDER_POLICY_NUMBER = "first-policy-number"
        private const val FIRST_SUB_ORDER_CONTRACT_NUMBER = "first-contract-number"
        private const val FIRST_SUB_ORDER_TYPE_INSURANCE = "first-test-insurance"
        private const val FIRST_SUB_ORDER_INSURANCE_PROGRAM = "first-test-program"

        private const val SECOND_SUB_ORDER_POLICY_NUMBER = "second-policy-number"
        private const val SECOND_SUB_ORDER_CONTRACT_NUMBER = "second-contract-number"
        private const val SECOND_SUB_ORDER_TYPE_INSURANCE = "second-test-insurance"
        private const val SECOND_SUB_ORDER_INSURANCE_PROGRAM = "second-test-program"

        private val RETURN_URL = URI.create("http://www.sogaz.com")

        private val validFileQR = FileQR(QR_CONTENT, MediaTypeValue.IMAGE_PNG_VALUE)

        private val firstSubOrder = SubOrder(
            policyNumber = FIRST_SUB_ORDER_POLICY_NUMBER,
            contractNumber = FIRST_SUB_ORDER_CONTRACT_NUMBER,
            typeInsurance = FIRST_SUB_ORDER_TYPE_INSURANCE,
            insuranceProgram = FIRST_SUB_ORDER_INSURANCE_PROGRAM
        )

        private val secondSubOrder = SubOrder(
            policyNumber = SECOND_SUB_ORDER_POLICY_NUMBER,
            contractNumber = SECOND_SUB_ORDER_CONTRACT_NUMBER,
            typeInsurance = SECOND_SUB_ORDER_TYPE_INSURANCE,
            insuranceProgram = SECOND_SUB_ORDER_INSURANCE_PROGRAM
        )

        private val firstAccount = SubOrderInfo(
            policyNumber = FIRST_SUB_ORDER_POLICY_NUMBER,
            contractNumber = FIRST_SUB_ORDER_CONTRACT_NUMBER,
            typeInsurance = FIRST_SUB_ORDER_TYPE_INSURANCE,
            insuranceProgram = FIRST_SUB_ORDER_INSURANCE_PROGRAM
        )

        private val secondAccount = SubOrderInfo(
            policyNumber = SECOND_SUB_ORDER_POLICY_NUMBER,
            contractNumber = SECOND_SUB_ORDER_CONTRACT_NUMBER,
            typeInsurance = SECOND_SUB_ORDER_TYPE_INSURANCE,
            insuranceProgram = SECOND_SUB_ORDER_INSURANCE_PROGRAM
        )

        private val pageInfoRequestParams = PageInfoRequestParams(
            RETURN_URL
        )
    }

    private lateinit var infoPageService: InfoPageService

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var configDataDao: ConfigDataDao

    @MockK
    private lateinit var registerPaymentService: RegisterPaymentService

    @MockK
    private lateinit var qrCodeService: QRCodeService

    private val orderMapper = Mappers.getMapper(OrderMapper::class.java)

    private lateinit var validOrderUUID: UUID
    private lateinit var inValidOrderUUID: UUID
    private lateinit var payURITemplate: URI

    @BeforeEach
    fun beforeEach() {
        validOrderUUID = UUID.randomUUID()
        inValidOrderUUID = UUID.randomUUID()
        payURITemplate = initPayURITemplate()
        val testOrder = initTestOrder(validOrderUUID)

        infoPageService = initInfoPageService()

        every { orderDao.findById(validOrderUUID) } returns testOrder
        every { orderDao.findById(inValidOrderUUID) } throws InnerException(OrderServiceTest.TRACE_ID, "DB error")
        every { qrCodeService.generateFileQR(any<URI>()) } returns null
        setSbpActiveConfigValue(false)
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid urlPayBank`() {
        validOrderUUID
            .run { infoPageService.getInfo(this, pageInfoRequestParams) }
            .run(::assertThat)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(payURITemplate, DataOrderPaymentPageInfo::urlPayBank)
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid accounts`() {
        validOrderUUID
            .run { infoPageService.getInfo(this, pageInfoRequestParams) }
            .run(::assertThat)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(ORDER_TEST_AMOUNT, DataOrderPaymentPageInfo::premiumAmount)
            .returns(listOf(firstAccount, secondAccount), DataOrderPaymentPageInfo::accounts)
    }

    @Test
    fun `getOrderPaymentPageInfo should return empty accounts for order without suborders`() {
        every { orderDao.findById(validOrderUUID) } returns Order(id = validOrderUUID)

        validOrderUUID
            .run { infoPageService.getInfo(this, pageInfoRequestParams) }
            .run(::assertThat)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns("", DataOrderPaymentPageInfo::premiumAmount)
            .returns(emptyList(), DataOrderPaymentPageInfo::accounts)
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr get from qr-generator-service`() {
        setSbpActiveConfigValue(true)
        every { qrCodeService.generateFileQR(any<URI>()) } returns validFileQR

        validOrderUUID
            .run { infoPageService.getInfo(this, pageInfoRequestParams) }
            .run(::assertThat)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .extracting<PaySbp>(DataOrderPaymentPageInfo::paySbp)
            .returns(validFileQR, PaySbp::fileQR)

        verify { qrCodeService.generateFileQR(any<URI>()) }
        verify(inverse = true) { qrCodeService.requestFileQRFromBank(any()) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr requested from bank`() {
        setSbpActiveConfigValue(true)
        every { registerPaymentService.register(any(), PaymentTypeEnum.SBP, any()) }.returns(Payment(qrcId = "qr-id"))
        every { qrCodeService.requestFileQRFromBank(any()) } returns validFileQR

        validOrderUUID
            .run { infoPageService.getInfo(this, pageInfoRequestParams) }
            .run(::assertThat)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .extracting<PaySbp>(DataOrderPaymentPageInfo::paySbp)
            .returns(validFileQR, PaySbp::fileQR)

        verify { qrCodeService.generateFileQR(any<URI>()) }
        verify { qrCodeService.requestFileQRFromBank(any()) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo without sbp pay info`() {
        validOrderUUID
            .run { infoPageService.getInfo(this, pageInfoRequestParams) }
            .run(::assertThat)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(null) { it.paySbp?.urlPay }
            .returns(null) { it.paySbp?.fileQR }

        verify(inverse = true) { qrCodeService.generateFileQR(any<URI>()) }
        verify(inverse = true) { qrCodeService.requestFileQRFromBank(any()) }
    }

    private fun setSbpActiveConfigValue(value: Boolean) =
        every { configDataDao.findByKey(PaymentServiceImpl.SBP_ACTIVE_CONFIG_NAME, Boolean::class) }
            .returns(value)

    private fun initInfoPageService() =
        InfoPageServiceImpl(
            orderDao = orderDao,
            configDataDao = configDataDao,
            registerPaymentService = registerPaymentService,
            qrCodeService = qrCodeService,
            orderMapper = orderMapper,
        ).apply {
            cardPayBaseUri = BASE_PAYMENT_CARD_PAY_PATH
            sbpPayBaseUri = BASE_PAYMENT_SBP_PAY_PATH
        }

    private fun initTestOrder(id: UUID) =
        Order(
            id = id,
            premiumAmount = ORDER_TEST_AMOUNT,
        ).apply {
            subOrders.add(firstSubOrder)
            subOrders.add(secondSubOrder)
        }

    private fun initPayURITemplate() =
        URI.create("$BASE_PAYMENT_CARD_PAY_PATH$validOrderUUID?urlToReturn=$RETURN_URL&depersonalization=false")
}
