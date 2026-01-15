package ru.sogaz.site.paymentService.service.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import ru.sogaz.site.paymentService.config.MetricsConfig.Companion.DEFAULT_TAG
import ru.sogaz.site.paymentService.config.MetricsConfig.Companion.GPB_CALLBACK_FAIL
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.service.GpbCallbackMetricService

class GpbCallbackMetricServiceImpl(
    private val meterRegistry: MeterRegistry,
    private var gpbCallbackSuccessCounter: Counter,
) : GpbCallbackMetricService {
    private fun incrementFailMetric(extResultCode: String?) {
        val counter = meterRegistry.counter(GPB_CALLBACK_FAIL, "ExtResultCode", extResultCode ?: DEFAULT_TAG)
        counter.increment()
    }

    private fun incrementSuccessMetric() {
        gpbCallbackSuccessCounter.increment()
    }

    override fun setMetric(requestDto: GpbCallbackRequest) {
        requestDto.resultCode?.let { resultCode ->
            when (resultCode) {
                1 -> incrementSuccessMetric()
                2 -> incrementFailMetric(extResultCode = requestDto.extResultCode)
            }
        }
    }
}
