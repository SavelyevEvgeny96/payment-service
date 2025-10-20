package ru.sogaz.site.paymentService.dao

interface ConfigDataDao {
    fun getBankInfoFromConfigData(
        traceId: String,
        valueNameInfo: String,
    ): String
}
