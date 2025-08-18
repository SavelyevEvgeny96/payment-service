package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.CallbackRequest
import ru.sogaz.site.paymentService.dto.CallbackResponse
import ru.sogaz.siter.models.resonses.Response

interface CallbackService {
    fun processCallback(request: CallbackRequest): Response<CallbackResponse>
}
