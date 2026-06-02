package ru.sogaz.site.paymentService.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.service.metrics.GpbCallbackMetricServiceImpl

@Configuration
class MetricsConfig {
    companion object {
        const val GPB_CALLBACK_FAIL = "gpb_callback_fail"
        const val GPB_CALLBACK_SUCCESS = "gpb_callback_success"
        const val SUCCESS = "success"
        const val FAIL = "fail"
        const val DEFAULT_TAG = "default"
    }

    @Bean
    fun gpbCallbackMetrics(meterRegistry: MeterRegistry): GpbCallbackMetricServiceImpl {
        val gpbCallbackSuccessCounter: Counter = meterRegistry.counter(GPB_CALLBACK_SUCCESS)
        return GpbCallbackMetricServiceImpl(meterRegistry, gpbCallbackSuccessCounter)
    }
}
