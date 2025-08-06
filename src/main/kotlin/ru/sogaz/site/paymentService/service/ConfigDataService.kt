package ru.sogaz.site.paymentService.service

interface ConfigDataService {
    fun getCodeLength(traceId: String): Int
}
