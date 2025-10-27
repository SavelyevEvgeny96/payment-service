package ru.sogaz.site.paymentService.validation

import com.auth0.jwt.exceptions.JWTDecodeException
import ru.sogaz.site.jwt.starter.dto.JwtClaims
import ru.sogaz.site.jwt.starter.service.JwtService
import ru.sogaz.site.paymentService.dao.ClientSystemDao
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.exceptions.ForbiddenBusinessException
import ru.sogaz.site.paymentService.exceptions.UnauthorizedBusinessException
import ru.sogaz.site.paymentService.loggerFor

class PermissionValidator(
    private val clientSystemDao: ClientSystemDao,
    private val jwtService: JwtService,
) {
    companion object {
        private const val CLIENT_ID = "clientId"
    }

    private val logger = loggerFor(javaClass)

    @Throws(UnauthorizedBusinessException::class, ForbiddenBusinessException::class)
    fun checkPermission(authorizationHeader: String?): ClientSystem? =
        try {
            jwtService
                .getClaims(authorizationHeader)
                .run(::verifyClientSystem)
        } catch (ex: JWTDecodeException) {
            logger.error(ex.message)
            throw UnauthorizedBusinessException()
        }

    private fun verifyClientSystem(jwtClaims: JwtClaims) =
        jwtClaims
            .getClaim<String>(CLIENT_ID)
            .run(clientSystemDao::findBySystemCode)
            .also { if (it == null) throw ForbiddenBusinessException() }
}
