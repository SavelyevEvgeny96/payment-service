package ru.sogaz.site.paymentService.scheduler

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.sogaz.site.filterStarter.services.RequestInfo.SERVICE_NAME
import ru.sogaz.site.filterStarter.services.RequestInfo.TRACE_ID
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dao.findByKey
import ru.sogaz.site.paymentService.entity.WaitingPayment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.PaymentStatusService
import java.util.UUID

@Component
class WaitingPaymentsStatusesScheduler(
    private val configDataDao: ConfigDataDao,
    private val waitingPaymentDao: WaitingPaymentDao,
    private val paymentStatusService: PaymentStatusService,
) {
    private val logger = loggerFor(javaClass)

    companion object {
        const val CHUNK_SIZE_PARAM_NAME = "waitingPaymentsStatusCheckChunkSize"
        const val DEFAULT_CHUNK_SIZE = 200
        const val LOG_CHUNK_SIZE_CONFIG_ERROR =
            "Ошибка при получении размера единоразово проверяемых платежей, " +
                "будет использовано значение по умолчанию - ${CallbackPaymentsStatusesScheduler.DEFAULT_CHUNK_SIZE}"
        const val LOG_CRITICAL_TASK_ERROR = "Критическая ошибка в фоновой задаче проверки платежей"
    }

    @Scheduled(cron = "\${crons.waitingPaymentsCheck}")
    @SchedulerLock(name = "WaitingPaymentsStatusesScheduler_updateWaitingPaymentsStatuses", lockAtMostFor = "PT1M")
    fun updateWaitingPaymentsStatuses() {
        MDC.put(TRACE_ID, UUID.randomUUID().toString())
        MDC.put(SERVICE_NAME, "payment-service")
        runCatching { runTask() }
            .also(::logResult)
        MDC.clear()
    }

    private fun runTask() =
        findChunkSizeConfig()
            .run(::getEarliestPaymentsChunkFromQueue)
            .also(::logWaitingPayments)
            .onEach(::updatePaymentStatus)

    private fun getEarliestPaymentsChunkFromQueue(chunkSize: Int) = waitingPaymentDao.findTopNEarliestUpdated(chunkSize)

    private fun updatePaymentStatus(waitingPayment: WaitingPayment) = paymentStatusService.updateStatus(waitingPayment)

    private fun findChunkSizeConfig(): Int =
        try {
            configDataDao.findByKey(CHUNK_SIZE_PARAM_NAME)
        } catch (ex: Exception) {
            logger.error(LOG_CHUNK_SIZE_CONFIG_ERROR, ex)
            DEFAULT_CHUNK_SIZE
        }

    private fun logWaitingPayments(payments: List<WaitingPayment>) {
        logger.info("Для обновления статусов выгружено ${payments.size} платежей ожидающих оплаты")
    }

    private fun logResult(result: Result<Any>) {
        if (result.isFailure) {
            logger.info(LOG_CRITICAL_TASK_ERROR, result.exceptionOrNull())
        }
    }
}
