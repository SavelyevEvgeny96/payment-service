package ru.sogaz.site.paymentService.api.doc.v2.requestSchema

import io.swagger.v3.oas.annotations.media.Schema

data class StraightRedirectSchema(
    @field:Schema(description = "Ссылка для редиректа после оплаты", example = "https://www.sogaz.ru/")
    val urlToReturn: String? = null,
)
