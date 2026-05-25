package ru.sogaz.site.paymentService.model.v2.web.request.pay
import com.fasterxml.jackson.annotation.JsonInclude
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest

sealed class PayOperationRequest : OperationRequest() {
    open val description: String = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    open val payItems: LinkedHashMap<String, String> = LinkedHashMap()
    open val depersonalization: Boolean = false
}
