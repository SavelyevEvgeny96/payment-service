package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.ERROR_CLIENT_SYSTEM_NOT_FOUND

class GetClientSystemDaoImpl(private val clientSystemRepository: ClientSystemRepository) : GetClientSystemDao {
    companion object{
        const val LOG_CLIENT_SYSTEM_NOT_FOUND =
            "Не удалось найти систему клиента для externalSystemCode: {} и TraceId: {}"
    }
    private val logger = loggerFor(javaClass)
    override fun getClientSystem(traceId: String, externalSystemCode: String): ClientSystem {
        return try {
            clientSystemRepository.findByExternalSystemCode(externalSystemCode)
        } catch (e: Exception) {
            logger.error(e, LOG_CLIENT_SYSTEM_NOT_FOUND, externalSystemCode, traceId,
            )
            throw InnerException(traceId, ERROR_CLIENT_SYSTEM_NOT_FOUND)
        }
    }
}