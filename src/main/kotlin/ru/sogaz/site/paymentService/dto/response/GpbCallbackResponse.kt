package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "register-payment-response")
data class GpbCallbackResponse(
    @param:JacksonXmlProperty(localName = "result")
    val result: CallbackResult,
)

data class CallbackResult(
    @param:JacksonXmlProperty(localName = "code")
    val code: Int,
    @param:JacksonXmlProperty(localName = "desc")
    val desc: String,
)
