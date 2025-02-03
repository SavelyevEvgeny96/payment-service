package ru.sogaz.site.paymentService.validation.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import ru.sogaz.site.paymentService.service.TokenService
import java.io.IOException

class TokenValidationFilter(private val tokenService: TokenService) : OncePerRequestFilter() {

    companion object {
        const val MISSING_HEADER = "Потерян заголовок Authorizationс или TraceId "
    }
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authorizationHeader = request.getHeader("Authorization")
        val traceIdHeader = request.getHeader("TraceId")

        if (authorizationHeader.isNullOrEmpty() || traceIdHeader.isNullOrEmpty()) {
            throw ServletException(MISSING_HEADER)
        }
        tokenService.validateToken(authorizationHeader)
        chain.doFilter(request, response)
    }
}