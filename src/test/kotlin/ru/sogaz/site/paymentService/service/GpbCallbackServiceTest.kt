package ru.sogaz.site.paymentService.service

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.callback.GpbCallbackServiceImpl
import java.util.UUID

class GpbCallbackServiceTest {
    private val signatureVerifier = mock<SignatureVerifier>()
    private val paymentDao = mock<PaymentDao>()
    private val orderDao = mock<OrderDao>()
    private val paymentOperationHistoryDao = mock<PaymentOperationHistoryDao>()
    private val apiConfigProperties = mock<ApiConfigProperties>()
    private val callbackPaymentDao = mock<CallbackPaymentDao>()
    private val httpServletRequest = mock<HttpServletRequest>()

    private val service =
        GpbCallbackServiceImpl(
            paymentDao,
            orderDao,
            paymentOperationHistoryDao,
            signatureVerifier,
            apiConfigProperties,
            callbackPaymentDao,
        )

    private val testRequest =
        GpbCallbackRequest(
            trxId = "ZLZA2BRR45VP6YF0",
            merchId = "GCS_merchant2",
            resultCode = 1,
            amount = "1004",
            accountId = "4EEA096E50948B54C32C32455AD6BCAC",
            orderId = "ord-da03ea907e5b",
            rrn = "240524141650",
            authCode = "410545",
            srcType = "CARD",
            maskedPan = "444499xxxxxx6000",
            isFullyAuthenticated = "Y",
            transmissionDateTime = "1202104556",
            discountType = "CREDIT",
            discountAmount = "10000",
            paymentSystem = "VISA",
            issuerName = "Sber",
            ts = "20240524 14:16:50",
            signature =
                "Q6WBwrZr%2BW%2BcBlZ1pBgdRcOgr2aAh8cognmOjK7iqmcl5VWIQb0x%2Br8M9COnvaNsQlbuWkc62e2EdxfHqr" +
                    "6SLcLduOxPQhCan6qKDkAMUuPZYbS1ycISo",
        )

    @Test
    fun `processCallback should return success response when all steps are successful`() {
        val ordersId = "ORDER_123"
        val order =
            Order().apply {
                id = UUID.randomUUID()
            }

        val payment =
            Payment().apply {
                id = UUID.randomUUID()
                paymentBankId = testRequest.trxId
                this.order = order
            }
        `when`(signatureVerifier.verifySignature(any(), any())).thenReturn(true)
        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
        `when`(orderDao.findById(any())).thenReturn(order)

        val response = service.processCallback(testRequest, httpServletRequest)

        assertThat(response.body).contains("<code>1</code>")
        verify(paymentDao).save(payment)
    }

    @Test
    fun `processCallback should fail when orderId is Null`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                order = null
            }

        `when`(signatureVerifier.verifySignature(testRequest, httpServletRequest)).thenReturn(true)
        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)

        val response = service.processCallback(testRequest, httpServletRequest)

        assertThat(response.body).contains("<code>2</code>")
    }

    @Test
    fun `processCallback should fail when orderId not Found`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                order = Order().apply { id = UUID.randomUUID() }
            }

        `when`(signatureVerifier.verifySignature(testRequest, httpServletRequest)).thenReturn(true)
        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
        `when`(payment.order?.id?.let { orderDao.findById(it) }).thenReturn(null)

        val response = service.processCallback(testRequest, httpServletRequest)

        assertThat(response.body).contains("<code>2</code>")
    }
}
