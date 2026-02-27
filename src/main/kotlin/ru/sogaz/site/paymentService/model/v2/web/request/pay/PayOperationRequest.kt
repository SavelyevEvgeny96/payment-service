package ru.sogaz.site.paymentService.model.v2.web.request.pay

import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest

sealed class PayOperationRequest : OperationRequest() {
    abstract val description: String
    abstract val payItems: LinkedHashMap<String, String>
    abstract val depersonalization: Boolean
}
