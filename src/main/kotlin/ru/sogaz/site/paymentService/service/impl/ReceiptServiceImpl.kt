package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dto.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentService.dto.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ReceiptProperty
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import java.time.LocalDateTime

class ReceiptServiceImpl(
    private val receiptProperty: ReceiptProperty,
    private val restTemplate: RestTemplate,
    private val subOrderRepository: SubOrderRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val objectMapper: ObjectMapper
) : ReceiptService {
    private val logger = loggerFor(javaClass)

    override fun generateReceipt(
        order: Order,
        traceId: String,
    ) {
        val url = receiptProperty.receiptUrl

        val subOrders = subOrderRepository.findAllByOrderId(order)

        fun String.toReceiptAmount(): Double =
            try {
                val cleanAmount = this.replace(" ", "").replace(",", ".")
                val amount = cleanAmount.toDouble()

                val parts = cleanAmount.split(".")
                if (parts.size > 1 && parts[1].length > 2) {
                    throw IllegalArgumentException("Дробная часть должна содержать не более 2 знаков")
                }
                if (parts[0].length > 8) {
                    throw IllegalArgumentException("Целая часть должна содержать не более 8 знаков")
                }
                amount
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Некорректный формат суммы: $this", e)
            }

        val receiptItems = subOrders.mapNotNull { subOrder ->
            val itemName = buildString {
                append("Страховая премия")
                if (!subOrder.policyNumber.isNullOrEmpty()) {
                    append(" по страховому полису № ${subOrder.policyNumber}")
                }
                if (subOrder.contractId.isNotEmpty()) {
                    append(" по страховому договору № ${subOrder.contractId}")
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
                    vat = PaymentReceiptCreateRequest.VatRequest(type = "none")
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
                        phone = order.recipientPhone,
                        name = order.policyholder,
                    ),
                    userId = order.recipientUserId,
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
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    HttpEntity(requestBody, headers),
                    String::class.java,
                ).body ?: ""

            val responseBody = objectMapper.readValue(response, PaymentReceiptCreateResponse::class.java)

            when {
                responseBody.status == "SUCCESS" -> {
                    logger.info("Чек успешно сгенерирован для заказа ${order.code}. TraceId: $traceId")
                    saveReceiptOperationHistory(order, responseBody.data.externalId, traceId)
                }

                responseBody.status == "FAILED" -> {
                    logger.error("Ошибка при генерации чека. TraceId: $traceId")
                    throw InnerException(traceId, "Ошибка данных чека")
                }

                else -> {
                    logger.error("Ошибка API при генерации чека. Status: ${responseBody.code}. TraceId: $traceId")
                    throw InnerException(traceId, "Ошибка сервиса чеков: ${responseBody.code}")
                }
            }
        } catch (e: Exception) {
            logger.info("Ошибка при генерации чека для заказа ${order.code}. TraceId: $traceId", e)
            saveFailedReceiptOperationHistory(order, e.message, traceId)
            throw InnerException(traceId, "Ошибка при генерации чека: ${e.message}")
        }
    }

    private fun saveReceiptOperationHistory(
        order: Order,
        externalId: String?,
        traceId: String,
    ) {
        val actionType = actionTypeRepository.findByActionName("RECEIPT_GENERATED")
        val subOrder = subOrderRepository.findFirstByOrderId(order)
        operationHistoryRepository.save(
            PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = LocalDateTime.now(),
            ),
        )
    }

    private fun saveFailedReceiptOperationHistory(
        order: Order,
        error: String?,
        traceId: String,
    ) {
        val actionType = actionTypeRepository.findByActionName("RECEIPT_GENERATION_ERROR")
        val subOrder = subOrderRepository.findFirstByOrderId(order)
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
