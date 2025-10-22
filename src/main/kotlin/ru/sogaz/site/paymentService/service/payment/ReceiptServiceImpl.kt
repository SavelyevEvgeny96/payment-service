package ru.sogaz.site.paymentService.service.payment

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.payment.receipt.client.api.PaymentReceiptControllerApi
import ru.sogaz.site.payment.receipt.client.model.ClientInfo
import ru.sogaz.site.payment.receipt.client.model.PaymentItemRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest
import ru.sogaz.site.payment.receipt.client.model.VatRequest
import ru.sogaz.site.paymentService.dao.ChequeSentDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.ChequeSent
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.PaymentMethodEnum
import ru.sogaz.site.paymentService.enums.PaymentObjectEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.ReceiptService
import java.time.LocalDateTime

@Service
class ReceiptServiceImpl(
    private val paymentDao: PaymentDao,
    private val subOrderDao: SubOrderDao,
    private val chequeSentDao: ChequeSentDao,
    private val operationHistoryDao: PaymentOperationHistoryDao,
    private val paymentReceiptControllerApi: PaymentReceiptControllerApi,
) : ReceiptService {
    private val logger = loggerFor(javaClass)

    companion object {
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

        val payment = paymentDao.findByOrder(order)
        val subOrders = subOrderDao.getAllSubOrderListByOrderId(order)

        val receiptItems = subOrders!!.map(::buildReceiptItem)
        val totalAmount = order.makeReceiptAmount()
        val requestBody = PaymentReceiptCreateRequest()

        order.recipientEmail?.let { ClientInfo().email(it) }?.let { requestBody.client(it) }
        requestBody.items.add(receiptItems.first().items.first())
        requestBody.payments.add(
            totalAmount.let {
                PaymentPaymentRequest().apply {
                    type = "1"
                    sum = it
                }
            },
        )
        requestBody.system = "Atol"
        requestBody.total = totalAmount
        requestBody.version = "v4"

        try {
            val response = paymentReceiptControllerApi.createPaymentCheck(requestBody)

            when (response.status) {
                StatusEnum.SUCCESS.value -> {
                    logger.info(LOG_RECEIPT_SUCCESS.format(order.id, traceId))
                    payment?.paymentBankId?.let { handleReceiptSuccess(order, it) }
                }

                StatusEnum.FAILED.value -> {
                    logger.error(LOG_RECEIPT_FAILED.format(traceId))
                    payment?.paymentBankId?.let { handleReceiptError(order, it) }
                    throw InnerException(traceId, ERROR_DATA_RECEIPT)
                }

                else -> {
                    logger.error(LOG_RECEIPT_API_ERROR.format(response.status, traceId))
                    payment?.paymentBankId?.let {
                        handleReceiptError(
                            order,
                            it,
                        )
                    }
                    throw InnerException(traceId, ERROR_RECEIPT + response.code)
                }
            }
        } catch (e: Exception) {
            logger.info(LOG_RECEIPT_ERROR.format(order.id, traceId), e)
            if (payment?.paymentBankId != null) {
                handleReceiptError(order, payment.paymentBankId)
            }
            throw InnerException(traceId, ERROR_RECEIPT_GENERATION + e.message)
        }
    }

    private fun buildReceiptItem(subOrder: SubOrder): PaymentReceiptCreateRequest {
        val itemName = buildItemName(subOrder)
        val amount = subOrder.makeReceiptAmount()
        return buildPaymentItemRequest(itemName, amount)
            .run { PaymentReceiptCreateRequest().addItemsItem(this) }
    }

    private fun buildPaymentItemRequest(
        itemName: String,
        amount: Double,
    ) = PaymentItemRequest().apply {
        name = itemName
        price = amount
        quantity = 1.00
        sum = amount
        paymentMethod = PaymentMethodEnum.FULL_PAYMENT.value
        paymentObject = PaymentObjectEnum.PAYMENT_OBJECT_SERVICE.value
        vat = VatRequest().type("none")
    }

    private fun buildItemName(subOrder: SubOrder): String =
        buildString {
            append(ITEM_NAME_PREFIX)
            if (!subOrder.policyNumber.isNullOrEmpty()) {
                append(POLICY_NUMBER_PREFIX.format(subOrder.policyNumber))
            }
            if (!subOrder.contractId.isNullOrEmpty()) {
                append(CONTRACT_ID_PREFIX.format(subOrder.contractId))
            }
        }

    private fun handleReceiptError(
        order: Order,
        paymentBankId: String?,
    ) {
        paymentBankId?.let {
            val payment = paymentDao.findByPaymentBankId(it)
            payment.chequeName = "NOT_SENT"
            paymentDao.save(payment)
        }
        saveFailedReceiptOperationHistory(order)
        saveChequeSentRecord(paymentBankId, false)
    }

    private fun handleReceiptSuccess(
        order: Order,
        paymentBankId: String,
    ) {
        val payment = paymentDao.findByPaymentBankId(paymentBankId)
        payment.chequeName = "SENT"
        paymentDao.save(payment)
        saveReceiptOperationHistory(order)
    }

    private fun saveChequeSentRecord(
        paymentBankId: String?,
        success: Boolean,
    ) {
        chequeSentDao.save(
            ChequeSent(
                paymentBankId = paymentBankId,
                status = if (success) StatusEnum.SUCCESS.value else StatusEnum.FAILED.value,
                dateCreate = LocalDateTime.now(),
                dateUpdate = LocalDateTime.now(),
            ),
        )
    }

    private fun saveReceiptOperationHistory(order: Order) {
        operationHistoryDao.saveForOrder(order, ActionType.ORDER_PAID.value)
    }

    private fun saveFailedReceiptOperationHistory(order: Order) {
        operationHistoryDao.saveForOrder(order, ActionType.PAYMENT_ERROR.value)
    }

    private fun SubOrder.makeReceiptAmount(): Double =
        try {
            this.premiumAmount!!
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
                .also(::checkAmount)
        } catch (e: NumberFormatException) {
            throw InnerException(getTraceId(), ERROR_INCORRECT_SUM + this)
        }

    private fun Order.makeReceiptAmount(): Double =
        try {
            this.premiumAmount
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
                .also(::checkAmount)
        } catch (e: NumberFormatException) {
            throw InnerException(getTraceId(), ERROR_INCORRECT_SUM + this)
        }

    private fun checkAmount(amount: Double) {
        val parts = amount.toString().split(".")
        if (parts.size > 1 && parts[1].length > 2) {
            throw InnerException(getTraceId(), ERROR_FRACTION_SUM)
        }
        if (parts[0].length > 8) {
            throw InnerException(getTraceId(), ERROR_HOLL_SUM)
        }
    }
}
