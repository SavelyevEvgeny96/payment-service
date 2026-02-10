package ru.sogaz.site.paymentService.service.v2.order

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderMapper
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderMapperImpl
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.service.v2.order.impl.IdempotentOrderServiceImpl
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(IdempotentOrderMapperImpl::class)
class IdempotentOrderServiceTest {
    @MockK
    private lateinit var idempotentOrderDao: IdempotentOrderDao

    @MockK
    private lateinit var idempotentOrderOperationDao: IdempotentOrderOperationDao

    @Autowired
    private lateinit var idempotentOrderMapper: IdempotentOrderMapper

    private lateinit var idempotentOrderService: IdempotentOrderService

    @RelaxedMockK
    private lateinit var payOperationRequest: CardPayOperationRequest

    @BeforeEach
    fun beforeEach() {
        idempotentOrderService =
            IdempotentOrderServiceImpl(
                idempotentOrderDao,
                idempotentOrderOperationDao,
                idempotentOrderMapper,
            )

        every { idempotentOrderDao.save(any()) } returnsArgument 0
        every { idempotentOrderOperationDao.save(any()) } returnsArgument 0

        every { payOperationRequest.orderId } returns UUID.randomUUID()
    }

    val mapper = jacksonObjectMapper()

    val json =
        """
        {
          "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "description": "string",
          "amount": 10.56,
          "payItems": {
            "param1": "string"
          },
          "params": {
            "urlToReturn": "http://www.sogaz.ru",
            "urlToReturnS": "http://www.sogaz.ru",
            "urlToReturnF": "http://www.sogaz.ru",
            "depersonalization": false,
            "saveCard": true
          }
        }
        """.trimIndent()

    @Test
    fun `asdf`() {
        val req = mapper.readValue<CardPayOperationRequest>(json)
        println(req)
    }

    @Test
    fun `saveOperation should create new Order and new operation if not found current order`() {
        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns null

        idempotentOrderService.saveOperation(payOperationRequest)

        verify { idempotentOrderDao.save(any()) }
        verify { idempotentOrderOperationDao.save(any()) }
    }

    @Test
    fun `saveOperation should return current Order and new operation`() {
        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns mockk()

        idempotentOrderService.saveOperation(payOperationRequest)

        verify(exactly = 0) { idempotentOrderDao.save(any()) }
        verify { idempotentOrderOperationDao.save(any()) }
    }

    @Test
    fun `saveOperation should return operation with types from request`() {
        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns mockk()

        val savedOperation = idempotentOrderService.saveOperation(payOperationRequest)

        assertThat(savedOperation)
            .returns(payOperationRequest.operationType, IdempotentOrderOperation::operationType)
            .returns(payOperationRequest.paymentType, IdempotentOrderOperation::paymentType)
    }

    @Test
    fun `saveOperation should return operation with depersonalization same as request`() {
        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns mockk()
        every { payOperationRequest.params.depersonalization } returns true

        val savedOperation = idempotentOrderService.saveOperation(payOperationRequest)

        assertThat(savedOperation)
            .returns(true, IdempotentOrderOperation::depersonalization)
    }
}
