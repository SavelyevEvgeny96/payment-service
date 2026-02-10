package ru.sogaz.site.paymentService.service.v2.bank.gpb

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClientV2
import ru.sogaz.site.paymentService.mapper.payment.CardDetailsMapperImpl
import ru.sogaz.site.paymentService.mapper.payment.PaymentStatusMapperImpl
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.GpbRequestMapperImpl
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.GpbResponseMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.GpbResponseMapperImpl
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbCardAccountProperties

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [GpbRequestMapperImpl::class, GpbResponseMapperImpl::class, CardDetailsMapperImpl::class, PaymentStatusMapperImpl::class])
class GpbCardPayIntegrationTest {
    companion object {
        private const val TOKEN = "TEST TOKEN"
        private const val PAYMENT_PAGE = "PAYMENT PAGE"

        private const val MAIN_PORTAL_ID = "MAIN_P"
        private const val MAIN_MERCHANT_ID = "MAIN_M"

        private const val DEPERSONALIZED_PORTAL_ID = "DEP_M"
        private const val DEPERSONALIZED_MERCHANT_ID = "DEP_P"
    }

    @MockK
    private lateinit var gpbCardClient: GpbCardPaymentClientV2

    @Autowired
    private lateinit var requestMapper: GpbRequestMapper

    @Autowired
    private lateinit var responseMapper: GpbResponseMapper

    @RelaxedMockK
    private lateinit var cardAccountProperties: GpbCardAccountProperties

    private lateinit var gpbCardPayIntegration: GpbCardPayIntegration

    @RelaxedMockK
    private lateinit var cardPayOperationRequest: CardPayOperationRequest

    @RelaxedMockK
    private lateinit var gpbPayCardResponse: GpbPayCardResponse

    @BeforeEach
    fun beforeEach() {
        gpbCardPayIntegration =
            GpbCardPayIntegration(
                gpbCardClient,
                requestMapper,
                responseMapper,
                cardAccountProperties,
            )

        initMockAccountProperties()
        initMockGpbPayCardResponse()

        every { gpbCardClient.getToken(any()).token } returns TOKEN
        every { gpbCardClient.cardPayment(any(), TOKEN, any()) } returns gpbPayCardResponse
    }

    @Test
    fun `pay should call correct cardAccountData based on depersonalization`() {
        every { cardPayOperationRequest.params.depersonalization } returns false

        val result = gpbCardPayIntegration.pay(cardPayOperationRequest)

        verify { gpbCardClient.getToken(MAIN_PORTAL_ID) }
        verify { gpbCardClient.cardPayment(MAIN_PORTAL_ID, TOKEN, any()) }

        assertThat(result)
            .returns(TOKEN, BankPaymentPageData::id)
    }

    @Test
    fun `pay should call correct cardAccountData based on depersonalization if it is true`() {
        every { cardPayOperationRequest.params.depersonalization } returns true

        val result = gpbCardPayIntegration.pay(cardPayOperationRequest)

        verify { gpbCardClient.getToken(DEPERSONALIZED_PORTAL_ID) }
        verify { gpbCardClient.cardPayment(DEPERSONALIZED_PORTAL_ID, TOKEN, any()) }

        assertThat(result)
            .returns(TOKEN, BankPaymentPageData::id)
    }

    private fun initMockAccountProperties() {
        every { cardAccountProperties.main.portalId } returns MAIN_PORTAL_ID
        every { cardAccountProperties.main.merchantId } returns MAIN_MERCHANT_ID
        every { cardAccountProperties.depersonalized.portalId } returns DEPERSONALIZED_PORTAL_ID
        every { cardAccountProperties.depersonalized.merchantId } returns DEPERSONALIZED_MERCHANT_ID
    }

    private fun initMockGpbPayCardResponse() {
        every { gpbPayCardResponse.token } returns TOKEN
        every { gpbPayCardResponse.options.paymentPageUrl } returns PAYMENT_PAGE
    }
}
