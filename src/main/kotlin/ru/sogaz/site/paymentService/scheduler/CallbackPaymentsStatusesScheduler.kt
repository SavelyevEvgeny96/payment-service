package ru.sogaz.site.paymentService.scheduler

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.sogaz.site.filterStarter.services.RequestInfo.SERVICE_NAME
import ru.sogaz.site.filterStarter.services.RequestInfo.TRACE_ID
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.findByKey
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.PaymentStatusService
import java.util.UUID

@Component
@ConditionalOnProperty(name = ["api.version"], havingValue = "v1")
class CallbackPaymentsStatusesScheduler(
    private val configDataDao: ConfigDataDao,
    private val callbackPaymentDao: CallbackPaymentDao,
    private val paymentStatusService: PaymentStatusService,
) {
    private val logger = loggerFor(javaClass)

    companion object {
        const val CHUNK_SIZE_PARAM_NAME = "callbackPaymentsStatusCheckChunkSize"
        const val DEFAULT_CHUNK_SIZE = 30
        const val LOG_CHUNK_SIZE_CONFIG_ERROR =
            "Ошибка при получении размера единоразово проверяемых платежей, " +
                "будет использовано значение по умолчанию - $DEFAULT_CHUNK_SIZE"
        const val LOG_CRITICAL_TASK_ERROR = "Критическая ошибка в фоновой задаче проверки платежей"
    }

    @Scheduled(cron = "\${crons.callbackPaymentsCheck}")
    @SchedulerLock(
        name = "CallbackPaymentsStatusesScheduler_updateCallbackPaymentsStatuses",
        lockAtLeastFor = "PT1S",
        lockAtMostFor = "PT1M",
    )
    fun updateCallbackPaymentsStatuses() {
        MDC.put(TRACE_ID, UUID.randomUUID().toString())
        MDC.put(SERVICE_NAME, "payment-service")
        runCatching { runTask() }
            .also(::logResult)
        MDC.clear()
    }

    private fun runTask() =
        findChunkSizeConfig()
            .run(::getEarliestPaymentsChunkFromQueue)
            .onEach(::updatePaymentStatus)

    private fun getEarliestPaymentsChunkFromQueue(chunkSize: Int) = callbackPaymentDao.findLimitEarliestUpdated(chunkSize)

    private fun updatePaymentStatus(callbackPayment: CallbackPayment) = paymentStatusService.updateStatus(callbackPayment)

    private fun findChunkSizeConfig(): Int =
        try {
            configDataDao.findByKey(CHUNK_SIZE_PARAM_NAME)
        } catch (ex: Exception) {
            logger.error(LOG_CHUNK_SIZE_CONFIG_ERROR, ex)
            DEFAULT_CHUNK_SIZE
        }

    private fun logResult(result: Result<Any>) {
        if (result.isFailure) {
            logger.debug(LOG_CRITICAL_TASK_ERROR, result.exceptionOrNull())
        }
    }
}
