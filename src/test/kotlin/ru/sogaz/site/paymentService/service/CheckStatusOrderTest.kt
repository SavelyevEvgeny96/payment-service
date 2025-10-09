package ru.sogaz.site.paymentService.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.payment.PaymentStatusCheckerServiceImpl

class CheckStatusOrderTest {
    private val paymentRepository: PaymentRepository = mock()

    private val restTemplate: WebConfigRestTemplate = mock()
    private val subOrderDao: SubOrderDao = mock()
    private val orderDao: OrderDao = mock()
    private val apiConfigProperty: ApiConfigProperties = mock()
    private val receiptService: ReceiptService = mock()
    private val rabbitTemplate: RabbitTemplate = mock()
    private val objectMapper: ObjectMapper = mock()
    private val rabbit: RabbitProperties = mock()
    private val paymentDao: PaymentDao = mock()
    private val operationHistoryDao: PaymentOperationHistoryDao = mock()
    private val historyService: HistoryService = mock()

    private val service =
        PaymentStatusCheckerServiceImpl(
            paymentDao,
            restTemplate,
            apiConfigProperty,
            receiptService,
            rabbitTemplate,
            objectMapper,
            rabbit,
            subOrderDao,
            operationHistoryDao,
            orderDao,
            historyService,
        )
    private val errorCodePaidFor = 1001
    private val errorCodeNotAvailable = 2002

    @Test
    fun `should throw BusinessException with errorCodePaidFor when status SUCCESS`() {
        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(OrderStatus.SUCCESS, errorCodePaidFor, errorCodeNotAvailable)
            }

        assert(ex.getErrorCode() == errorCodePaidFor)
    }

    @Test
    fun `should throw BusinessException with errorCodeNotAvailable when status OVERDUE`() {
        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(OrderStatus.OVERDUE, errorCodePaidFor, errorCodeNotAvailable)
            }

        assert(ex.getErrorCode() == errorCodeNotAvailable)
    }

    @Test
    fun `should throw BusinessException with errorCodeNotAvailable when status MARKEDDEL`() {
        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(OrderStatus.MARKEDDEL, errorCodePaidFor, errorCodeNotAvailable)
            }

        assert(ex.getErrorCode() == errorCodeNotAvailable)
    }

    @Test
    fun `should not throw exception when status NEW`() {
        service.checkStatusOrder(OrderStatus.NEW, errorCodePaidFor, errorCodeNotAvailable)
    }
}
