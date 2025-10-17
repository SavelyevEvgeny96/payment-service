package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "register-payment-response")
data class GpbCallbackResponse(
    @JacksonXmlProperty(localName = "result")
    val result: CallbackResult,
)

data class CallbackResult(
    @JacksonXmlProperty(localName = "code")
    val code: Int,
    @JacksonXmlProperty(localName = "desc")
    val desc: String,
)
