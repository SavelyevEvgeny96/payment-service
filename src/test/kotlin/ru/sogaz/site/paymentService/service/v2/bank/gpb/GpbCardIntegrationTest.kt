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
import ru.sogaz.site.paymentService.clients.gpb.GpbCardClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.common.GpbPayStatusMapperImpl
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapperImpl
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardDetailMapperImpl
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardResponseMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardResponseMapperImpl
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbCardAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.impl.GpbCardCardIntegrationImpl

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(
    value = [GpbRequestMapperImpl::class, GpbCardResponseMapperImpl::class, GpbCardDetailMapperImpl::class, GpbPayStatusMapperImpl::class],
)
class GpbCardIntegrationTest {
    companion object {
        private const val TOKEN = "TEST TOKEN"
        private const val PAYMENT_PAGE = "PAYMENT PAGE"

        private const val MAIN_PORTAL_ID = "MAIN_P"
        private const val MAIN_MERCHANT_ID = "MAIN_M"

        private const val DEPERSONALIZED_PORTAL_ID = "DEP_M"
        private const val DEPERSONALIZED_MERCHANT_ID = "DEP_P"
    }

    @MockK
    private lateinit var gpbCardClient: GpbCardClient

    @Autowired
    private lateinit var requestMapper: GpbRequestMapper

    @Autowired
    private lateinit var responseMapper: GpbCardResponseMapper

    @RelaxedMockK
    private lateinit var cardAccountProperties: GpbCardAccountProperties

    private lateinit var gpbCardPayIntegration: GpbCardCardIntegrationImpl

    @RelaxedMockK
    private lateinit var cardPayOperationRequest: CardPayOperationRequest

    @RelaxedMockK
    private lateinit var cardRecurrentPayOperationRequest: CardRecurrentOperationRequest

    @RelaxedMockK
    private lateinit var gpbPayCardResponse: GpbPayCardResponse

    @RelaxedMockK
    private lateinit var gpbCardPayDetailsResponse: GpbCardPayDetailsResponse

    @BeforeEach
    fun beforeEach() {
        gpbCardPayIntegration =
            GpbCardCardIntegrationImpl(
                gpbCardClient,
                requestMapper,
                responseMapper,
                cardAccountProperties,
            )

        initMockAccountProperties()
        initMockGpbPayCardResponse()
        initMockGpbCardPayDetailsResponse()

        every { gpbCardClient.getToken(any()).token } returns TOKEN
        every { gpbCardClient.cardPayment(any(), TOKEN, any()) } returns gpbPayCardResponse
        every { gpbCardClient.cardRecurrentPayment(any(), TOKEN, any()) } returns gpbCardPayDetailsResponse
    }

    @Test
    fun `pay should call correct cardAccountData based on depersonalization`() {
        every { cardPayOperationRequest.depersonalization } returns false

        val authorizedCardTrxData = gpbCardPayIntegration.authorize(cardPayOperationRequest)
        val result = gpbCardPayIntegration.cardPay(cardPayOperationRequest, authorizedCardTrxData)

        verify { gpbCardClient.getToken(MAIN_PORTAL_ID) }
        verify { gpbCardClient.cardPayment(MAIN_PORTAL_ID, TOKEN, any()) }

        assertThat(result)
            .returns(TOKEN, BankPaymentPageData::paymentBankId)
    }

    @Test
    fun `pay should call correct cardAccountData based on depersonalization if it is true`() {
        every { cardPayOperationRequest.depersonalization } returns true

        val authorizedCardTrxData = gpbCardPayIntegration.authorize(cardPayOperationRequest)
        val result = gpbCardPayIntegration.cardPay(cardPayOperationRequest, authorizedCardTrxData)

        verify { gpbCardClient.getToken(DEPERSONALIZED_PORTAL_ID) }
        verify { gpbCardClient.cardPayment(DEPERSONALIZED_PORTAL_ID, TOKEN, any()) }

        assertThat(result)
            .returns(TOKEN, BankPaymentPageData::paymentBankId)
    }

    @Test
    fun `recurrent pay should call correct cardAccountData based on depersonalization`() {
        every { cardPayOperationRequest.depersonalization } returns false

        val authorizedCardTrxData = gpbCardPayIntegration.authorize(cardPayOperationRequest)
        val result = gpbCardPayIntegration.recurrentPay(cardRecurrentPayOperationRequest, authorizedCardTrxData)

        verify { gpbCardClient.getToken(MAIN_PORTAL_ID) }
        verify { gpbCardClient.cardRecurrentPayment(MAIN_PORTAL_ID, TOKEN, any()) }

        assertThat(result)
            .returns(TOKEN, BankOperationDetails::bankId)
    }

    @Test
    fun `recurrent pay should call correct cardAccountData based on depersonalization if it is true`() {
        every { cardPayOperationRequest.depersonalization } returns true

        val authorizedCardTrxData = gpbCardPayIntegration.authorize(cardPayOperationRequest)
        val result = gpbCardPayIntegration.recurrentPay(cardRecurrentPayOperationRequest, authorizedCardTrxData)

        verify { gpbCardClient.getToken(DEPERSONALIZED_PORTAL_ID) }
        verify { gpbCardClient.cardRecurrentPayment(DEPERSONALIZED_PORTAL_ID, TOKEN, any()) }

        assertThat(result)
            .returns(TOKEN, BankOperationDetails::bankId)
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

    private fun initMockGpbCardPayDetailsResponse() {
        every { gpbCardPayDetailsResponse.id } returns TOKEN
    }
}
