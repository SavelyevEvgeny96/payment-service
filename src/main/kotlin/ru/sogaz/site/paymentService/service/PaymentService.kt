package ru.sogaz.site.paymentService.service

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.config.ApiConfig
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
@Service
class PaymentService(

    private val paymentRepository: PaymentRepository,
    private val apiConfig: ApiConfig,
    private val bankRepository: BankRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val orderRepository: OrderRepository
) {
    companion object {
        const val STATUS_CODE_SUCCESS = 1101500200
        const val SUCCESS = "success"
    }

    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param paymentRequest Данные о платеже
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект Payment, содержащий информацию о платежном запросе
     */
    fun createPayment(paymentRequest: PaymentRequest, traceId: String): PaymentResponse {
        val result = PaymentResponse(traceId)
        val paymentId = generateUniquePaymentId()
        val paymentCode = generateUniquePaymentCode()
        val clientSystem = clientSystemRepository.findByCode(paymentRequest.externalSystemCode)
        val bank = if (paymentRequest.bank != null) {
            bankRepository.findById(paymentRequest.bank.toLong()).orElseThrow()
        } else {
            bankRepository.findFirstByOrderById()
        }
        val paymentPageUrl = "${apiConfig.paymentUrl}$paymentId}"
        val order = Order(
            paymentId = paymentId,
            code = paymentCode,
            bank = bank,
            premiumAmount = paymentRequest.premiumAmount,
            recipientEmail = paymentRequest.recipientEmail,
            recipientPhone = paymentRequest.recipientPhone,
            operationId = paymentRequest.operationId,
            paymentEndDate = paymentRequest.paymentEndDate,
            clientSystem = clientSystem,
            docType = paymentRequest.docType,
            policyId = paymentRequest.policyId,
            policyNumber = paymentRequest.policyNumber,
            contractId = paymentRequest.contractId,
            needReceipt = paymentRequest.needReceipt ?: false,
            policyholder = paymentRequest.policyholder,
            policyholderDoc = paymentRequest.policyholderDoc,
            managerEmail = paymentRequest.managerEmail,
            urlToReturn = paymentRequest.urlToReturn,
            urlToDecline = paymentRequest.urlToDecline,
            customURL = paymentRequest.customURL,
            paymentPageUrl = paymentPageUrl,
            hash = paymentRequest.hash,
            createDate = getCurrentDateMoscow(),
            updateDate = getCurrentDateMoscow(),
            orderStatus = null
        )
        orderRepository.save(order)
        result.data.code = paymentCode
        result.data.url = paymentPageUrl
        result.code = STATUS_CODE_SUCCESS
        result.status = SUCCESS
        return result

    }
}

private fun generateUniquePaymentCode(): String {
    return UUID.randomUUID().toString().replace("-", "").take(12).uppercase(Locale.getDefault())
}

fun getCurrentDateMoscow(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000")
    sdf.timeZone = TimeZone.getTimeZone("Europe/Moscow") // Устанавливаем московский часовой пояс
    return sdf.format(Date())
}

private fun generateUniquePaymentId(): String {
    return UUID.randomUUID().toString()
}

