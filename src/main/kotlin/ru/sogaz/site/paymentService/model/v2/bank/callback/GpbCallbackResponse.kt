package ru.sogaz.site.paymentService.model.v2.bank.callback

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "register-payment-response")
data class GpbCallbackResponse(
    @param:JacksonXmlProperty(localName = "result")
    val result: CallbackResult,
) {
    constructor() : this(CallbackResult(1, "OK"))
    constructor(error: String) : this(CallbackResult(2, error))
}

data class CallbackResult(
    @param:JacksonXmlProperty(localName = "code")
    val code: Int,
    @param:JacksonXmlProperty(localName = "desc")
    val desc: String,
)
