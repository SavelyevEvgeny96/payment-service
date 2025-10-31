package ru.sogaz.site.paymentService.dao

inline fun <reified T : Any> ConfigDataDao.findByKey(key: String): T = this.findByKey(key, T::class)
