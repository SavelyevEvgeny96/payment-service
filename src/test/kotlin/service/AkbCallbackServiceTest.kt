package service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dto.AkbCallbackRequest
import ru.sogaz.site.paymentService.dto.ResponseStatusPay
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.impl.AkbCallbackServiceImpl
import ru.sogaz.siter.models.resonses.Response
import java.util.Optional

class AkbCallbackServiceTest {
    private val paymentRepository = Mockito.mock<PaymentRepository>()
    private val orderRepository = Mockito.mock<OrderRepository>()
    private val operationHistoryRepository = Mockito.mock<PaymentOperationHistoryRepository>()
    private val paymentStatusService = Mockito.mock<PaymentStatusCheckerService>()
    private val paymentStatusRepository = Mockito.mock<PaymentStatusRepository>()
    private val actionTypeRepository = Mockito.mock<ActionTypeRepository>()
    private val clientSystemRepository = Mockito.mock<ClientSystemRepository>()

    private val service =
        AkbCallbackServiceImpl(
            paymentRepository,
            paymentStatusRepository,
            actionTypeRepository,
            clientSystemRepository,
            orderRepository,
            operationHistoryRepository,
            paymentStatusService,
        )

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

        val responseStatus =
            Response<ResponseStatusPay>(
                status = "SUCCESS",
                code = 200,
                traceId = "222",
                data = ResponseStatusPay("222", true),
            )
        val externalSystem = ClientSystem(1, "PAY", "Test")

        val action = ActionType(1, "Получение CALLBACK от АКБ Россия")
        Mockito.`when`(paymentRepository.findByPaymentBankId(testRequest.bankId)).thenReturn(payment)
        Mockito.`when`(actionTypeRepository.findByActionName("Получение CALLBACK от АКБ Россия")).thenReturn(action)
        Mockito.`when`(clientSystemRepository.findByExternalSystemCode("PAY")).thenReturn(externalSystem)
        Mockito.`when`(payment.orderId?.id?.let { orderRepository.findById(it) }).thenReturn(Optional.of(order))
        Mockito.`when`(operationHistoryRepository.save(ArgumentMatchers.any())).thenAnswer { it.arguments[0] }
        Mockito.`when`(paymentStatusService.getStatus(testRequest.bankId, "222")).thenReturn(responseStatus)

        val response = service.processCallback(testRequest)

        Assertions.assertThat(response.data).isNotNull
        Mockito.verify(paymentRepository).save(payment)
    }

    @Test
    fun `processCallback should fail when orderId is Null`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.bankId
                orderId = null
            }

        Mockito.`when`(paymentRepository.findByPaymentBankId(testRequest.bankId)).thenReturn(payment)

        val response = assertThrows<InnerException> { service.processCallback(testRequest) }

        Assertions.assertThat(response.message).isNotNull()
    }

    @Test
    fun `processCallback should fail when orderId not Found`() {
        val payment =
            Payment().apply {
                paymentBankId = testRequest.bankId
                orderId = Order().apply { id = 999L }
            }

        Mockito.`when`(paymentRepository.findByPaymentBankId(testRequest.bankId)).thenReturn(payment)
        Mockito.`when`(payment.orderId?.id?.let { orderRepository.findById(it) }).thenReturn(Optional.empty())

        val response = assertThrows<InnerException> { service.processCallback(testRequest) }

        Assertions.assertThat(response.message).isNotNull()
    }
}
