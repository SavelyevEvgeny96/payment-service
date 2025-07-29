package service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.dto.ResponseStatusPay
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.impl.GpbCallbackServiceImpl
import ru.sogaz.siter.models.resonses.Response
import java.util.Optional

class GpbCallbackServiceTest {
    private val paymentRepository = mock<PaymentRepository>()
    private val orderRepository = mock<OrderRepository>()
    private val operationHistoryRepository = mock<PaymentOperationHistoryRepository>()
    private val paymentStatusService = mock<PaymentStatusCheckerService>()
    private val signatureVerifier = mock<SignatureVerifier>()
    private val paymentStatusCheckerService = mock<PaymentStatusCheckerService>()

    private val service =
        GpbCallbackServiceImpl(
            paymentRepository,
            orderRepository,
            operationHistoryRepository,
            paymentStatusService,
            signatureVerifier,
        )

    private val testRequest =
        GpbCallbackRequest(
            trxId = "ZLZA2BRR45VP6YF0",
            merchId = "GCS_merchant2",
            merchantTrx = null,
            resultCode = 1,
            extResultCode = null,
            amount = "1004",
            accountId = "4EEA096E50948B54C32C32455AD6BCAC",
            orderId = "ord-da03ea907e5b",
            rrn = "240524141650",
            authCode = "410545",
            srcType = "CARD",
            signature = "test_signature",
            rawQueryString = "test_query_string",
        )

    @Test
    fun `processCallback should return success response when all steps are successful`() {
        val order =
            Order().apply {
                id = 1L
                orderId = "ORDER_123"
            }

        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                orderId = order
            }

        val responseStatus =
            Response<ResponseStatusPay>(
                status = "SUCCESS",
                code = 200,
                traceId = "222",
                data = ResponseStatusPay("222", true),
            )

        `when`(signatureVerifier.verifySignature(testRequest.rawQueryString, testRequest.signature)).thenReturn(true)
        `when`(paymentRepository.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
        `when`(payment.orderId?.id?.let { orderRepository.findById(it) }).thenReturn(Optional.of(order))
        `when`(operationHistoryRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(paymentStatusCheckerService.getStatus(testRequest.trxId, "222")).thenReturn(responseStatus)

        val response = service.processCallback(testRequest)

        assertThat(response.body).contains("<code>1</code>")
        verify(paymentRepository).save(payment)
    }

    @Test
    fun `processCallback should fail when orderId is Null`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                orderId = null
            }

        `when`(signatureVerifier.verifySignature(testRequest.rawQueryString, testRequest.signature)).thenReturn(true)
        `when`(paymentRepository.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)

        val response = service.processCallback(testRequest)

        assertThat(response.body).contains("<code>2</code>")
    }

    @Test
    fun `processCallback should fail when orderId not Found`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                orderId = Order().apply { id = 999L }
            }

        `when`(signatureVerifier.verifySignature(testRequest.rawQueryString, testRequest.signature)).thenReturn(true)
        `when`(paymentRepository.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
        `when`(payment.orderId?.id?.let { orderRepository.findById(it) }).thenReturn(Optional.empty())

        val response = service.processCallback(testRequest)

        assertThat(response.body).contains("<code>2</code>")
    }
}
