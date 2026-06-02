package ru.sogaz.site.paymentService.model.v2.web.request.common

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.validation.constraint.SogazDomain

data class RedirectParams(
    @field:Schema(description = "Ссылка для редиректа после оплаты", example = "https://www.sogaz.ru/")
    @field:SogazDomain
    val urlToReturn: String? = null,
    @field:Schema(description = "Ссылка для редиректа после успешной оплаты", example = "https://www.sogaz.ru/")
    @field:SogazDomain
    val urlToReturnS: String? = null,
    @field:Schema(description = "Ссылка для редиректа после неуспешной оплаты", example = "https://www.sogaz.ru/")
    @field:SogazDomain
    val urlToReturnF: String? = null,
)
