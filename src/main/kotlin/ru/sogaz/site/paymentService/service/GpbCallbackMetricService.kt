package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest

interface GpbCallbackMetricService {
    fun setMetric(requestDto: GpbCallbackRequest)
}