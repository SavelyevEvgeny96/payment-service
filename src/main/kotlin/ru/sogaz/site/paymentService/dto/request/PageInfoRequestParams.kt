package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageInfoRequestParams(
    @JsonProperty("urlToReturn")
    val urlToReturn: URI? = null,
    @JsonProperty("urlToReturnS")
    val urlToReturnS: URI? = null,
    @JsonProperty("urlToReturnF")
    val urlToReturnF: URI? = null,
    @JsonProperty("depersonalization")
    val depersonalization: Boolean = false,
)
