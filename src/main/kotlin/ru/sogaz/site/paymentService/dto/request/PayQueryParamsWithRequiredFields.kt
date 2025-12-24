package ru.sogaz.site.paymentService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import java.net.URI
import javax.validation.constraints.NotNull

data class PayQueryParamsWithRequiredFields(
    @param:Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты")
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturn: URI,
    @param:Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты")
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturnS: URI,
    @param:Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты")
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturnF: URI,
    override val depersonalization: Boolean = false,
) : PayQueryParams()
