package service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.impl.GpbCallbackServiceImpl

class GpbCallbackServiceTest {
    private val signatureVerifier = mock<SignatureVerifier>()
    private val paymentDao = mock<PaymentDao>()
    private val orderDao = mock<OrderDao>()
    private val paymentOperationHistoryDao = mock<PaymentOperationHistoryDao>()
    private val getPaymentStatusDao = mock<GetPaymentStatusDao>()
    private val getOrderStatusDao = mock<OrderStatusDao>()

    private val callbackAction = ActionType(1, "Заказ оплачен")
    private val payClientSystem = ClientSystem(1, "PAY", "Test")

    private val service =
        GpbCallbackServiceImpl(
            paymentDao,
            orderDao,
            paymentOperationHistoryDao,
            signatureVerifier,
            getPaymentStatusDao,
            getOrderStatusDao,
            callbackAction,
            payClientSystem,
        )

    private val testRequest =
        GpbCallbackRequest(
            trxId = "ZLZA2BRR45VP6YF0",
            signature = "test_signature",
        )

    @Test
    fun `processCallback should return success response when all steps are successful`() {
        val ordersId = "ORDER_123"
        val traceId = "222"

        val order =
            Order().apply {
                id = 1L
                orderId = ordersId
            }

        val payment =
            Payment().apply {
                id = 1
                paymentBankId = testRequest.trxId
                orderId = order
            }

        `when`(signatureVerifier.verifySignature(testRequest.signature)).thenReturn(true)
        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
        `when`(orderDao.getOrderId(traceId, ordersId)).thenReturn(order)

        val response = service.processCallback(testRequest, traceId)

        assertThat(response.body).contains("<code>1</code>")
        verify(paymentDao).save(payment)
    }

    @Test
    fun `processCallback should fail when orderId is Null`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                orderId = null
            }

        `when`(signatureVerifier.verifySignature(testRequest.signature)).thenReturn(true)
        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)

        val response = service.processCallback(testRequest, "222")

        assertThat(response.body).contains("<code>2</code>")
    }

    @Test
    fun `processCallback should fail when orderId not Found`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.trxId
                orderId = Order().apply { id = 999L }
            }

        `when`(signatureVerifier.verifySignature(testRequest.signature)).thenReturn(true)
        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
        `when`(payment.orderId?.orderId?.let { orderDao.getOrderId("222", it) }).thenReturn(null)

        val response = service.processCallback(testRequest, "222")

        assertThat(response.body).contains("<code>2</code>")
    }
}
