package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentService.dto.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentService.entity.ChequeSent
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ReceiptProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ChequeSentRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import java.time.LocalDateTime

class ReceiptServiceImpl(
    private val receiptProperty: ReceiptProperties,
    private val restTemplate: RestTemplate,
    private val subOrderDao: SubOrderDao,
    private val actionTypeRepository: ActionTypeRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val objectMapper: ObjectMapper,
    private val paymentRepository: PaymentRepository,
    private val chequeSentRepository: ChequeSentRepository,
) : ReceiptService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val RECEIPT_GENERATED_ACTION = "Заказ оплачен"
        const val RECEIPT_GENERATION_ERROR_ACTION = "Ошибка при совершени платежа"
        const val ITEM_NAME_PREFIX = "Страховая премия"
        const val POLICY_NUMBER_PREFIX = " по страховому полису № "
        const val CONTRACT_ID_PREFIX = " по страховому договору № "
        const val LOG_RECEIPT_SUCCESS = "Чек успешно сгенерирован для заказа %s. TraceId: %s"
        const val LOG_RECEIPT_FAILED = "Ошибка при генерации чека. TraceId: %s"
        const val LOG_RECEIPT_API_ERROR = "Ошибка API при генерации чека. Status: %s. TraceId: %s"
        const val LOG_RECEIPT_ERROR = "Ошибка при генерации чека для заказа %s. TraceId: %s"
        const val ERROR_DATA_RECEIPT = "Ошибка данных чека"
        const val ERROR_RECEIPT = "Ошибка сервиса чеков: "
        const val ERROR_RECEIPT_GENERATION = "Ошибка при генерации чека: "
        const val ERROR_FRACTION_SUM = "Дробная часть должна содержать не более 2 знаков"
        const val ERROR_HOLL_SUM = "Целая часть должна содержать не более 8 знаков"
        const val ERROR_INCORRECT_SUM = "Некорректный формат суммы: "
    }

    override fun generateReceipt(order: Order) {
        val traceId = getTraceId()
        val url = receiptProperty.receiptUrl

        val payment = paymentRepository.findByOrderId(order)

        val subOrders = subOrderDao.getAllSubOrderListByOrderId(order, traceId)

        fun String.toReceiptAmount(): Double =
            try {
                val cleanAmount = this.replace(" ", "").replace(",", ".")
                val amount = cleanAmount.toDouble()

                val parts = cleanAmount.split(".")
                if (parts.size > 1 && parts[1].length > 2) {
                    throw InnerException(traceId, ERROR_FRACTION_SUM)
                }
                if (parts[0].length > 8) {
                    throw InnerException(traceId, ERROR_HOLL_SUM)
                }
                amount
            } catch (e: NumberFormatException) {
                throw InnerException(traceId, ERROR_INCORRECT_SUM + this)
            }

        val receiptItems =
            subOrders.mapNotNull { subOrder ->
                val itemName =
                    buildString {
                        append(ITEM_NAME_PREFIX)
                        if (!subOrder.policyNumber.isNullOrEmpty()) {
                            append(POLICY_NUMBER_PREFIX.format(subOrder.policyNumber))
                        }
                        if (!subOrder.contractId.isNullOrEmpty()) {
                            append(CONTRACT_ID_PREFIX.format(subOrder.contractId))
                        }
                    }

                subOrder.premiumAmount?.toReceiptAmount()?.let { premiumAmount ->
                    PaymentReceiptCreateRequest.PaymentItemRequest(
                        name = itemName,
                        price = premiumAmount,
                        quantity = 1.00,
                        sum = premiumAmount,
                        paymentMethod = "full_payment",
                        paymentObject = "service",
                        vat = PaymentReceiptCreateRequest.VatRequest(type = "none"),
                    )
                }
            }

        val totalAmount = order.premiumAmount?.toReceiptAmount()

        val requestBody =
            totalAmount?.let { it ->
                PaymentReceiptCreateRequest(
                    client =
                        PaymentReceiptCreateRequest.ClientInfo(
                            email = order.recipientEmail,
                        ),
                    items = receiptItems,
                    payments =
                        listOf(
                            totalAmount.let {
                                PaymentReceiptCreateRequest.PaymentPaymentRequest(
                                    type = "1",
                                    sum = it,
                                )
                            },
                        ),
                    system = "Atol",
                    total = it,
                    version = "v4",
                )
            }

        val headers =
            HttpHeaders().apply {
                set("TraceId", traceId)
                contentType = MediaType.APPLICATION_JSON
            }

        try {
            val response =
                restTemplate
                    .exchange(
                        url,
                        HttpMethod.POST,
                        HttpEntity(requestBody, headers),
                        PaymentReceiptCreateResponse::class.java,
                    )

            when (response.body?.status) {
                "SUCCESS" -> {
                    logger.info(LOG_RECEIPT_SUCCESS.format(order.orderId, traceId))
                    payment?.paymentBankId?.let { handleReceiptSuccess(order, it) }
                }

                "FAILED" -> {
                    logger.error(LOG_RECEIPT_FAILED.format(traceId))
                    payment?.paymentBankId?.let { handleReceiptError(order, it) }
                    throw InnerException(traceId, ERROR_DATA_RECEIPT)
                }

                else -> {
                    logger.error(LOG_RECEIPT_API_ERROR.format(response.body?.status, traceId))
                    payment?.paymentBankId?.let {
                        handleReceiptError(
                            order,
                            it,
                        )
                    }
                    throw InnerException(traceId, ERROR_RECEIPT + response.body?.code)
                }
            }
        } catch (e: Exception) {
            logger.info(LOG_RECEIPT_ERROR.format(order.orderId, traceId), e)
            if (payment?.paymentBankId != null) {
                handleReceiptError(order, payment.paymentBankId)
            }
            throw InnerException(traceId, ERROR_RECEIPT_GENERATION + e.message)
        }
    }

    private fun handleReceiptError(
        order: Order,
        paymentBankId: String?,
    ) {
        val payment = paymentRepository.findByPaymentBankId(paymentBankId)
        payment.chequeName = "NOT_SENT"
        payment.updateDate = LocalDateTime.now()
        paymentRepository.save(payment)
        saveFailedReceiptOperationHistory(order)
        saveChequeSentRecord(paymentBankId, false)
    }

    private fun handleReceiptSuccess(
        order: Order,
        paymentBankId: String,
    ) {
        val payment = paymentRepository.findByPaymentBankId(paymentBankId)
        payment.chequeName = "SENT"
        payment.updateDate = LocalDateTime.now()
        paymentRepository.save(payment)
        saveReceiptOperationHistory(order)
    }

    private fun saveChequeSentRecord(
        paymentBankId: String?,
        success: Boolean,
    ) {
        chequeSentRepository.save(
            ChequeSent(
                paymentBankId = paymentBankId,
                status = if (success) "SUCCESS" else "FAILED",
                dateCreate = LocalDateTime.now(),
                dateUpdate = LocalDateTime.now(),
            ),
        )
    }

    private fun saveReceiptOperationHistory(order: Order) {
        val actionType = actionTypeRepository.findByActionName(RECEIPT_GENERATED_ACTION)
        val subOrder = subOrderDao.getSubOrder(getTraceId(), order)
        operationHistoryRepository.save(
            PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = LocalDateTime.now(),
            ),
        )
    }

    private fun saveFailedReceiptOperationHistory(order: Order) {
        val actionType = actionTypeRepository.findByActionName(RECEIPT_GENERATION_ERROR_ACTION)
        val subOrder = subOrderDao.getSubOrder(getTraceId(), order)

        operationHistoryRepository.save(
            PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = LocalDateTime.now(),
            ),
        )
    }
}
