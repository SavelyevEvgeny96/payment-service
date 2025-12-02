package ru.sogaz.site.paymentService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import java.net.URI

data class PayQueryParams(
    @param:Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты")
    val urlToReturn: URI? = null,
    @param:Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты")
    val urlToReturnS: URI? = null,
    @param:Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты")
    val urlToReturnF: URI? = null,
    @param:Parameter(name = "depersonalization", description = "Флаг необходимости анонимизированной оплаты")
    val depersonalization: Boolean = false,
)
