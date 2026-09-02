package ru.sogaz.site.paymentService.service.v2.bank.gpb

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.sogaz.site.paymentService.clients.gpb.GpbGidClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbGidMapper
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid.GpbGidStatusRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidStatusResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidStatusResult
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.properties.gpb.GpbGidProperties
import ru.sogaz.site.paymentService.properties.gpb.GpbGidStatusRetryProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.impl.GpbGidIntegrationImpl
import java.time.Instant

class GpbGidIntegrationTest {
    companion object {
        private const val PAYMENT_BANK_ID = "pga-transaction-id"
        private const val PORTAL_ID = "portal-id"
        private const val DEPERSONALIZED_PORTAL_ID = "depersonalized-portal-id"
        private const val TIMESTAMP = 1_773_681_055_262L
    }

    private val client = mockk<GpbGidClient>()
    private val mapper = mockk<GpbGidMapper>()
    private val operation = mockk<IdempotentOrderOperation>()
    private lateinit var integration: GpbGidIntegrationImpl
    private lateinit var request: CapturingSlot<GpbGidStatusRequest>

    @BeforeEach
    fun setUp() {
        val properties =
            GpbGidProperties().apply {
                portalId = PORTAL_ID
                depersonalizedPortalId = DEPERSONALIZED_PORTAL_ID
                cookie = "session=value"
            }
        integration = GpbGidIntegrationImpl(client, properties, mapper, GpbGidStatusRetryProperties(0, 0))
        request = slot()
        every { operation.id } returns null
        every { operation.paymentBankId } returns PAYMENT_BANK_ID
        every { operation.depersonalization } returns false
    }

    @Test
    fun `status request uses operation transaction and configured portal`() {
        every { client.getPaymentStatus(any(), capture(request)) } returns response("UNKNOWN")

        val result = integration.payStatus(operation)

        assertThat(request.captured).isEqualTo(GpbGidStatusRequest(PAYMENT_BANK_ID, PORTAL_ID))
        assertThat(result.state).isEqualTo(OperationState.WAIT)
        verify {
            client.getPaymentStatus(
                match {
                    it["Accept-Language"] == "ru" &&
                        it["Content-Type"] == "application/json" &&
                        it["Cookie"] == "session=value" &&
                        it["X-Correlation-Id"]?.isNotBlank() == true
                },
                any(),
            )
        }
    }

    @Test
    fun `status request uses depersonalized portal`() {
        every { operation.depersonalization } returns true
        every { client.getPaymentStatus(any(), capture(request)) } returns response("INTERIM_SUCCESS")

        integration.payStatus(operation)

        assertThat(request.captured.portalId).isEqualTo(DEPERSONALIZED_PORTAL_ID)
    }

    @Test
    fun `successful result maps timestamp and omits error fields`() {
        every { client.getPaymentStatus(any(), any()) } returns response("SUCCESS", "IGNORED")

        val result = integration.payStatus(operation)

        assertThat(result.state).isEqualTo(OperationState.SUCCESS)
        assertThat(result.operationFinished).isEqualTo(Instant.ofEpochMilli(TIMESTAMP))
        assertThat(result.extendedCode).isNull()
        assertThat(result.errorText).isNull()
    }

    @Test
    fun `failed refund and declined results map to fail`() {
        listOf("FAILED", "REFUND", "DECLINED").forEach { status ->
            every { client.getPaymentStatus(any(), any()) } returns response(status, "DECLINED_BY_ISSUER")

            val result = integration.payStatus(operation)

            assertThat(result.state).isEqualTo(OperationState.FAIL)
            assertThat(result.extendedCode).isEqualTo("DECLINED_BY_ISSUER")
            assertThat(result.errorText).isEqualTo("Транзакция отклонена эмитентом")
        }
    }

    @Test
    fun `unknown extended code is used as error text`() {
        every { client.getPaymentStatus(any(), any()) } returns response("FAILED", "NEW_BANK_CODE")

        val result = integration.payStatus(operation)

        assertThat(result.extendedCode).isEqualTo("NEW_BANK_CODE")
        assertThat(result.errorText).isEqualTo("NEW_BANK_CODE")
    }

    @Test
    fun `mismatched transaction remains waiting`() {
        every { client.getPaymentStatus(any(), any()) } returns
            response("SUCCESS").copy(pgaTrxId = "different-transaction")

        val result = integration.payStatus(operation)

        assertThat(result.state).isEqualTo(OperationState.WAIT)
        assertThat(result.operationFinished).isNull()
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 500, 504])
    fun `configured bank errors are retried`(status: Int) {
        val properties =
            GpbGidProperties().apply {
                portalId = PORTAL_ID
                depersonalizedPortalId = DEPERSONALIZED_PORTAL_ID
            }
        integration = GpbGidIntegrationImpl(client, properties, mapper, GpbGidStatusRetryProperties(1, 0))
        every { client.getPaymentStatus(any(), any()) } throws feignException(status) andThen response("SUCCESS")

        val result = integration.payStatus(operation)

        assertThat(result.state).isEqualTo(OperationState.SUCCESS)
        verify(exactly = 2) { client.getPaymentStatus(any(), any()) }
    }

    private fun response(
        status: String,
        extendedCode: String? = null,
    ) = GpbGidStatusResponse(
        actualTimestamp = TIMESTAMP,
        pgaTrxId = PAYMENT_BANK_ID,
        state = "result",
        result = GpbGidStatusResult(status = status, extendedCode = extendedCode),
    )

    private fun feignException(status: Int): FeignException =
        FeignException.errorStatus(
            "getPaymentStatus",
            Response.builder()
                .status(status)
                .reason("bank error")
                .request(
                    Request.create(
                        Request.HttpMethod.POST,
                        "http://localhost/ecopay/api/v1/merchant/payment/state",
                        emptyMap(),
                        null,
                        Charsets.UTF_8,
                    ),
                )
                .build(),
        )
}
import feign.FeignException
import feign.Request
import feign.Response
