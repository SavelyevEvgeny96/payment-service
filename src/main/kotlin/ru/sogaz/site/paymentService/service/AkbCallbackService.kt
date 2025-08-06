package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.AkbCallbackRequest
import ru.sogaz.site.paymentService.dto.AkbCallbackResponse
import ru.sogaz.siter.models.resonses.Response

interface AkbCallbackService {
    fun processCallback(request: AkbCallbackRequest): Response<AkbCallbackResponse>
}
