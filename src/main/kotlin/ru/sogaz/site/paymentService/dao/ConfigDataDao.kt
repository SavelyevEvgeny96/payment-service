package ru.sogaz.site.paymentService.dao

import kotlin.reflect.KClass

interface ConfigDataDao {
    fun getConfigValueByKey(valueNameInfo: String): String

    fun <T : Any> findByKey(
        key: String,
        type: KClass<out T>,
    ): T
}
