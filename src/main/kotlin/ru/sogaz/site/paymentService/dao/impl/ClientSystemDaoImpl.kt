package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.ClientSystemDao
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.service.order.OrderServiceImpl.Companion.ERROR_CLIENT_SYSTEM_NOT_FOUND

@Repository
class ClientSystemDaoImpl(
    private val clientSystemRepository: ClientSystemRepository,
) : ClientSystemDao {
    companion object {
        const val LOG_CLIENT_SYSTEM_NOT_FOUND =
            "Не удалось найти систему клиента для externalSystemCode: {} и TraceId: {}"
    }

    private val logger = loggerFor(javaClass)

    override fun getClientSystem(
        traceId: String,
        externalSystemCode: String,
    ): ClientSystem =
        try {
            clientSystemRepository
                .findByExternalSystemCode(externalSystemCode)
                .run { this ?: throw InnerException(traceId, ERROR_CLIENT_SYSTEM_NOT_FOUND) }
        } catch (e: Exception) {
            logger.error(
                LOG_CLIENT_SYSTEM_NOT_FOUND,
                externalSystemCode,
                traceId,
                e,
            )
            throw InnerException(traceId, ERROR_CLIENT_SYSTEM_NOT_FOUND)
        }

    override fun findBySystemCode(externalSystemCode: String?): ClientSystem? =
        clientSystemRepository.findByExternalSystemCode(externalSystemCode)
}
