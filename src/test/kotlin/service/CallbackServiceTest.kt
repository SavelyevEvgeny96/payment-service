package service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.CallbackRequest
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.impl.CallbackServiceImpl

class CallbackServiceTest {
    private val paymentOperationHistoryDao = mock<PaymentOperationHistoryDao>()
    private val operationHistoryRepository = mock<PaymentOperationHistoryRepository>()
    private val callbackPaymentDao = mock<CallbackPaymentDao>()
    private val callbackPaymentStatus = PaymentStatus().apply { stateId = "CALLBACK_AKB" }
    private val callbackAction = ActionType(1, "Получение CALLBACK от АКБ Россия")
    private val payClientSystem = ClientSystem(1, "PAY", "Test")

    private val testRequest =
        CallbackRequest(
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
        val paymentDao =
            mock<PaymentDao>().apply {
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
            CallbackServiceImpl(
                paymentDao = paymentDao,
                orderDao = orderDao,
                callbackPaymentDao = callbackPaymentDao,
                callbackPaymentStatus = callbackPaymentStatus,
                callbackAction = callbackAction,
                payClientSystem = payClientSystem,
                paymentOperationHistoryDao = paymentOperationHistoryDao,
            )

        val response = service.processCallback(testRequest, "222")

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

        val paymentDao =
            mock<PaymentDao>().apply {
                `when`(getPaymentFromBankId(testRequest.bankId)).thenReturn(payment)
            }

        `when`(paymentDao.save(payment)).thenThrow(RuntimeException("DB error"))

        val orderDao =
            mock<OrderDao>().apply {
                `when`(getOrderId("222", "222")).thenReturn(Order())
            }

        val service =
            CallbackServiceImpl(
                paymentDao = paymentDao,
                orderDao = orderDao,
                callbackPaymentDao = callbackPaymentDao,
                callbackPaymentStatus = callbackPaymentStatus,
                callbackAction = callbackAction,
                payClientSystem = payClientSystem,
                paymentOperationHistoryDao = paymentOperationHistoryDao,
            )

        val exceptions = assertThrows<BusinessException> { service.processCallback(testRequest, "222") }

        assertThat(exceptions.getErrorCode()).isNotNull()
    }
}
