package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.request.GpbCallback

interface GpbCallbackMetricService {
    fun setMetric(gpbCallback: GpbCallback)
}
