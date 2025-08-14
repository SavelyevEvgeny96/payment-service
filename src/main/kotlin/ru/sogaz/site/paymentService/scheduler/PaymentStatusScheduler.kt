package ru.sogaz.site.paymentService.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import java.util.UUID

@Component
class PaymentStatusScheduler(
    private val paymentStatusCheckerService: PaymentStatusCheckerService,
    private val configDataRepository: ConfigDataRepository,
    private val paymentRepository: PaymentRepository,
) {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_BACKGROUND_TASK_START = "Запуск фоновой задачи проверки статусов платежей. ID операции: %s"
        const val LOG_UNPAID_PAYMENTS_FOUND = "Найдено %d неоплаченных платежей для проверки"
        const val LOG_PAYMENT_CHECK_ERROR = "Ошибка при проверке статуса платежа для заказа %s. ID операции: %s"
        const val LOG_CRITICAL_TASK_ERROR = "Критическая ошибка в фоновой задаче проверки платежей. ID операции: %s"
    }

    @Scheduled(fixedDelayString = "60000")
    fun checkUnpaidPayments() {
        val traceId = UUID.randomUUID().toString()
        logger.info(LOG_BACKGROUND_TASK_START.format(traceId))

        try {
            val periodPay = configDataRepository.findByParamName("periodPay").paramValue.toLong()

            val unpaidOrders = paymentRepository.findByStatuses(listOf("REG", "WAIT"))

            logger.info(LOG_UNPAID_PAYMENTS_FOUND.format(unpaidOrders.size))

            unpaidOrders.forEach { payment ->
                try {
                    paymentStatusCheckerService.processPaymentStatusCheck(payment, traceId)
                    Thread.sleep(periodPay)
                } catch (e: Exception) {
                    logger.info(LOG_PAYMENT_CHECK_ERROR.format(payment.paymentBankId, traceId), e)
                }
            }
        } catch (e: Exception) {
            logger.info(LOG_CRITICAL_TASK_ERROR.format(traceId), e)
        }
    }
}
