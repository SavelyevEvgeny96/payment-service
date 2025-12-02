package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.request.CallbackRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.service.callback.CallbackServiceImpl
import java.math.BigDecimal
import java.util.UUID

class CallbackServiceTest {
    private val paymentOperationHistoryDao = mockk<PaymentOperationHistoryDao>(relaxed = true)
    private val callbackPaymentDao = mockk<CallbackPaymentDao>(relaxed = true)

    private val testRequest = CallbackRequest(bankId = "ZLZA2BRR45VP6YF0")

    @Test
    fun `processCallback should return success response when all steps are successful`() {
        val payment =
            createTestPayment().apply {
                paymentBankId = testRequest.bankId
            }

        val paymentDao =
            mockk<PaymentDao>().apply {
                every { getPaymentFromBankId(testRequest.bankId) } returns payment
                every { save(payment) } returns payment
            }

        val orderDao =
            mockk<OrderDao>().apply {
                every { findById(any()) } returns payment.order
            }

        val service =
            CallbackServiceImpl(
                paymentDao = paymentDao,
                orderDao = orderDao,
                callbackPaymentDao = callbackPaymentDao,
                paymentOperationHistoryDao = paymentOperationHistoryDao,
            )

        val response = service.processCallback(testRequest)

        assertThat(response.data?.state).isEqualTo("OK")
    }

    @Test
    fun `should throw BusinessException when payment save fails`() {
        val payment =
            createTestPayment().apply {
                paymentBankId = testRequest.bankId
            }

        val paymentDao =
            mockk<PaymentDao>().apply {
                every { getPaymentFromBankId(testRequest.bankId) } returns payment
                every { save(payment) } throws RuntimeException("DB error")
            }

        val orderDao =
            mockk<OrderDao>().apply {
                every { findById(any()) } returns payment.order
            }

        val service =
            CallbackServiceImpl(
                paymentDao = paymentDao,
                orderDao = orderDao,
                callbackPaymentDao = callbackPaymentDao,
                paymentOperationHistoryDao = paymentOperationHistoryDao,
            )

        val ex =
            assertThrows<BusinessException> {
                service.processCallback(testRequest)
            }

        assertThat(ex.getErrorCode()).isNotNull
    }

    fun createTestPayment(
        bank: BankEnum = BankEnum.GPB,
        type: PaymentTypeEnum = PaymentTypeEnum.CARD,
        depersonalization: Boolean = false,
    ): Payment {
        val order =
            Order().apply {
                id = UUID.randomUUID()
                premiumAmount = BigDecimal("1000.00").toString()
                saveCard = false
            }

        return Payment(
            id = UUID.randomUUID(),
            order = order,
            bank = bank,
            type = type,
            depersonalization = depersonalization,
            urlToReturn =
                UrlToReturn(
                    urlToReturnS = null,
                    urlToReturnF = null,
                ),
        )
    }
}
