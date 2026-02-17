package ru.sogaz.site.paymentService.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.request.GpbCallback

interface GpbCallbackService {
    fun processCallback(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String>
}
