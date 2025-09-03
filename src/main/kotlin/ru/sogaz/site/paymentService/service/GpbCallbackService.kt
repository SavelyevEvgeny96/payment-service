package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest

interface GpbCallbackService {
    fun processCallback(request: GpbCallbackRequest): ResponseEntity<String>
}
