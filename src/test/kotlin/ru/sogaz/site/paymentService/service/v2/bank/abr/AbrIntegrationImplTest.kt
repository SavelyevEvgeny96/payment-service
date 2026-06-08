package ru.sogaz.site.paymentService.service.v2.bank.abr

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import ru.sogaz.site.paymentService.clients.abr.AbrCardClient
import ru.sogaz.site.paymentService.clients.abr.AbrSbpClient
import ru.sogaz.site.paymentService.dto.response.AbrOrderInfo
import ru.sogaz.site.paymentService.dto.response.AbrOrderResponse
import ru.sogaz.site.paymentService.dto.response.IpsRuData
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.request.AbrRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.response.AbrResponseMapper
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.v2.bank.abr.impl.AbrCardIntegrationImpl
import ru.sogaz.site.paymentService.service.v2.bank.abr.impl.AbrSbpIntegrationImpl
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AbrIntegrationImplTest {
    @MockK
    private lateinit var abrCardClient: AbrCardClient

    @MockK
    private lateinit var abrSbpClient: AbrSbpClient

    @Test
    fun `cardPay should map ABR order response to bank payment page data`() {
        every { abrCardClient.cardPayment(any()) } returns abrOrderResponse

        val result =
            AbrCardIntegrationImpl(abrCardClient, requestMapper(), Mappers.getMapper(AbrResponseMapper::class.java))
                .cardPay(cardRequest())

        assertThat(result.bank).isEqualTo(BankEnum.ABR)
        assertThat(result.paymentBankId).isEqualTo(TEST_ABR_ORDER_ID)
        assertThat(result.paymentPageUrl).isEqualTo(TEST_ABR_HPP_URL)
        assertThat(result.paymentPass).isEqualTo(TEST_ABR_PASSWORD)
    }

    @Test
    fun `sbpPay should register order set src token and prepare push transaction`() {
        every { abrSbpClient.sbpPayment(any()) } returns abrOrderResponse
        every { abrSbpClient.setSrcToken(TEST_ABR_ORDER_ID, TEST_ABR_PASSWORD, any()) } returns emptyMap()
        every { abrSbpClient.preparePushTran(TEST_ABR_ORDER_ID, TEST_ABR_PASSWORD, any()) } returns preparePushTranResponse

        val result =
            AbrSbpIntegrationImpl(abrSbpClient, requestMapper(), Mappers.getMapper(AbrResponseMapper::class.java))
                .sbpPay(sbpRequest())

        assertThat(result.bank).isEqualTo(BankEnum.ABR)
        assertThat(result.paymentBankId).isEqualTo(TEST_ABR_ORDER_ID)
        assertThat(result.paymentPageUrl).isEqualTo(TEST_ABR_QRC_PAYLOAD)
        assertThat(result.qrId).isEqualTo(TEST_ABR_ORDER_ID)
        assertThat(result.paymentPass).isEqualTo(TEST_ABR_PASSWORD)
        verify(exactly = 1) { abrSbpClient.setSrcToken(TEST_ABR_ORDER_ID, TEST_ABR_PASSWORD, any()) }
        verify(exactly = 1) { abrSbpClient.preparePushTran(TEST_ABR_ORDER_ID, TEST_ABR_PASSWORD, any()) }
    }

    private fun requestMapper(): AbrRequestMapper =
        Mappers
            .getMapper(AbrRequestMapper::class.java)
            .apply { apiConfigProperties = ApiConfigProperties().apply { backUrlS = TEST_BACK_URL } }

    private fun cardRequest() =
        CardPayOperationRequest(
            orderId = UUID.randomUUID(),
            description = TEST_DESCRIPTION,
            amount = BigDecimal.TEN,
            depersonalization = false,
            payerIp = null,
            params = RedirectParams(urlToReturnS = TEST_BACK_URL),
            saveCard = false,
        )

    private fun sbpRequest() =
        SbpPayOperationRequest(
            orderId = UUID.randomUUID(),
            description = TEST_DESCRIPTION,
            amount = BigDecimal.TEN,
            payerIp = "123",
            payItems = linkedMapOf(),
            params = RedirectParams(urlToReturnS = TEST_BACK_URL),
        )

    private companion object {
        const val TEST_ABR_ORDER_ID = "123"
        const val TEST_ABR_PASSWORD = "abr-password"
        const val TEST_ABR_HPP_URL = "payment-url?password=$TEST_ABR_PASSWORD"
        const val TEST_ABR_QRC_PAYLOAD = "abr-qrc-payload"
        const val TEST_DESCRIPTION = "description"
        const val TEST_BACK_URL = "https://www.sogaz.ru/"

        val abrOrderResponse =
            AbrOrderResponse(
                AbrOrderInfo(
                    id = TEST_ABR_ORDER_ID.toInt(),
                    hppUrl = TEST_ABR_HPP_URL,
                    password = TEST_ABR_PASSWORD,
                ),
            )
        val preparePushTranResponse =
            PreparePushTranResponse(
                specificByPm = mapOf("ipsRu" to IpsRuData(TEST_ABR_QRC_PAYLOAD, TEST_BACK_URL)),
            )
    }
}
