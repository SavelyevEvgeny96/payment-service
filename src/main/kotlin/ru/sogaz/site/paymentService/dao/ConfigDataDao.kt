package ru.sogaz.site.paymentService.dao

import java.util.UUID

interface ConfigDataDao {
    fun getCodeLength(traceId: String): Int

    fun generateUniquePaymentCode(traceId: String): String

    fun generateUniquePaymentId(): String = UUID.randomUUID().toString()
}
