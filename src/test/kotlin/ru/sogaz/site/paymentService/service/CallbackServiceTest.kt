package ru.sogaz.site.paymentService.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.request.CallbackRequest
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import ru.sogaz.site.paymentService.service.callback.CallbackServiceImpl
import java.util.Optional
import java.util.UUID

class CallbackServiceTest {
    private val paymentOperationHistoryDao = mock<PaymentOperationHistoryDao>()
    private val callbackPaymentDao = mock<CallbackPaymentDao>()
    private val payClientSystem = ClientSystem(1, "PAY", "Test")

    private val testRequest =
        CallbackRequest(
            bankId = "ZLZA2BRR45VP6YF0",
        )

    @Test
    fun `processCallback should return success response when all steps are successful`() {
        val order =
            Order().apply {
                id = UUID.randomUUID()
            }

        val payment =
            Payment().apply {
                paymentBankId = testRequest.bankId
                this.order = order
            }

        val paymentDao =
            mock<PaymentDao>().apply {
                `when`(getPaymentFromBankId(testRequest.bankId)).thenReturn(payment)
            }
        val orderDao =
            mock<OrderDao>().apply {
                `when`(findById(any())).thenReturn(Optional.of(order))
            }
        val service =
            CallbackServiceImpl(
                paymentDao = paymentDao,
                orderDao = orderDao,
                callbackPaymentDao = callbackPaymentDao,
                payClientSystem = ExternalSystemCodeEnum.PAY,
                paymentOperationHistoryDao = paymentOperationHistoryDao,
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
                order = Order().apply { id = UUID.randomUUID() }
            }

        val paymentDao =
            mock<PaymentDao>().apply {
                `when`(getPaymentFromBankId(testRequest.bankId)).thenReturn(payment)
            }

        `when`(paymentDao.save(payment)).thenThrow(RuntimeException("DB error"))

        val orderDao =
            mock<OrderDao>().apply {
                `when`(getOrderId("222")).thenReturn(Order())
            }

        val service =
            CallbackServiceImpl(
                paymentDao = paymentDao,
                orderDao = orderDao,
                callbackPaymentDao = callbackPaymentDao,
                payClientSystem = ExternalSystemCodeEnum.PAY,
                paymentOperationHistoryDao = paymentOperationHistoryDao,
            )

        val exceptions = assertThrows<BusinessException> { service.processCallback(testRequest) }

        assertThat(exceptions.getErrorCode()).isNotNull()
    }
}
