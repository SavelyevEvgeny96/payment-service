package ru.sogaz.site.paymentService.model.v2.web.request.pay

import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest

sealed class PayOperationRequest : OperationRequest() {
    open val description: String = ""
    open val payItems: LinkedHashMap<String, String> = LinkedHashMap()
    open val depersonalization: Boolean = false
}
