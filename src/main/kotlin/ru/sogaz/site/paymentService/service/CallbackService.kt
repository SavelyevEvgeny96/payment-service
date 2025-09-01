package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.request.CallbackRequest
import ru.sogaz.site.paymentService.dto.response.CallbackResponse
import ru.sogaz.siter.models.resonses.Response

interface CallbackService {
    fun processCallback(request: CallbackRequest): Response<CallbackResponse>
}
