package ru.sogaz.site.paymentService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import ru.sogaz.site.paymentService.validation.constraint.SogazDomain
import java.net.URI

open class PayQueryParams(
    @param:Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты")
    @field:SogazDomain
    open val urlToReturn: URI? = null,
    @param:Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты")
    @field:SogazDomain
    open val urlToReturnS: URI? = null,
    @param:Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты")
    @field:SogazDomain
    open val urlToReturnF: URI? = null,
    @param:Parameter(name = "depersonalization", description = "Флаг необходимости анонимизированной оплаты")
    open val depersonalization: Boolean = false,
)
