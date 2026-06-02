package ru.sogaz.site.paymentService.dao.impl

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import kotlin.reflect.KClass

@Repository
class ConfigDataDaoImpl(
    private val configDataRepository: ConfigDataRepository,
    private val objectMapper: ObjectMapper,
) : ConfigDataDao {
    companion object {
        private const val LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND = "Не найдено значение : "
        private const val INCORRECT_CONFIG_TYPE_ERROR_MESSAGE = "Не подходящий тип конфигурационного значения:"
    }

    private val logger = loggerFor(javaClass)

    override fun getConfigValueByKey(valueNameInfo: String): String =
        try {
            configDataRepository.findByParamName(valueNameInfo).paramValue
        } catch (e: Exception) {
            logger.error("$LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND $valueNameInfo")
            throw InnerException(getTraceId(), "$LOG_AND_ERROR_BANK_PRIORITY_NOT_FOUND$valueNameInfo")
        }

    override fun <T : Any> findByKey(
        key: String,
        type: KClass<out T>,
    ): T =
        try {
            val value: String = getConfigValueByKey(key)
            objectMapper.readValue(value, type.java)
        } catch (ex: JsonMappingException) {
            logger.error("$INCORRECT_CONFIG_TYPE_ERROR_MESSAGE $type")
            throw InnerException(getTraceId(), "$INCORRECT_CONFIG_TYPE_ERROR_MESSAGE $type")
        }
}
