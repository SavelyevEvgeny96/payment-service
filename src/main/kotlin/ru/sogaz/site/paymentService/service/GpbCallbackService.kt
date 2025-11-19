package ru.sogaz.site.paymentService.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest

interface GpbCallbackService {
    fun processCallback(
        requestDto: GpbCallbackRequest,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String>
}
