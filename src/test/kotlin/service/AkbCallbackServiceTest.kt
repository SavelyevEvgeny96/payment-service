package service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.AkbCallbackRequest
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.impl.AkbCallbackServiceImpl

class AkbCallbackServiceTest {
    private val paymentRepository = mock<PaymentRepository>()
    private val operationHistoryRepository = mock<PaymentOperationHistoryRepository>()
    private val getPaymentDao = mock<GetPaymentDao>()
    private val orderDao = mock<OrderDao>()
    private val callbackPaymentStatus = PaymentStatus().apply { stateId = "CALLBACK_AKB" }
    private val callbackAction = ActionType(1, "Получение CALLBACK от АКБ Россия")
    private val payClientSystem = ClientSystem(1, "PAY", "Test")

    private val testRequest =
        AkbCallbackRequest(
            bankId = "ZLZA2BRR45VP6YF0",
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
                paymentBankId = testRequest.bankId
                orderId = order
            }

        val paymentRepository = mock<PaymentRepository>()
        val getPaymentDao =
            mock<GetPaymentDao>().apply {
                `when`(getPaymentFromBankId(testRequest.bankId)).thenReturn(payment)
            }
        val orderDao =
            mock<OrderDao>().apply {
                `when`(getOrderId("222", "1L")).thenReturn(order)
            }
        val operationHistoryRepository =
            mock<PaymentOperationHistoryRepository>().apply {
                `when`(save(any())).thenAnswer { it.arguments[0] }
            }
        val service =
            AkbCallbackServiceImpl(
                paymentRepository = paymentRepository,
                operationHistoryRepository = operationHistoryRepository,
                getPaymentDao = getPaymentDao,
                orderDao = orderDao,
                callbackPaymentStatus = callbackPaymentStatus,
                callbackAction = callbackAction,
                payClientSystem = payClientSystem,
            )

        val response = service.processCallback(testRequest)

        assertThat(response.data).isNotNull
        assertThat(response.data?.state).isEqualTo("OK")
    }

    @Test
    fun `should throw exception when payment save fail`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.bankId
                orderId = Order().apply { id = 1L }
            }

        val getPaymentDao =
            mock<GetPaymentDao>().apply {
                `when`(getPaymentFromBankId(testRequest.bankId)).thenReturn(payment)
            }
        val paymentRepository =
            mock<PaymentRepository>().apply {
                `when`(save(any())).thenThrow(RuntimeException("DB error"))
            }
        val orderDao =
            mock<OrderDao>().apply {
                `when`(getOrderId("222", "222")).thenReturn(Order())
            }

        val service =
            AkbCallbackServiceImpl(
                paymentRepository = paymentRepository,
                operationHistoryRepository = operationHistoryRepository,
                getPaymentDao = getPaymentDao,
                orderDao = orderDao,
                callbackPaymentStatus = callbackPaymentStatus,
                callbackAction = callbackAction,
                payClientSystem = payClientSystem,
            )

        val exceptions = assertThrows<InnerException> { service.processCallback(testRequest) }

        assertThat(exceptions.message).isNotNull()
    }
}
