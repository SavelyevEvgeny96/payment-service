package ru.sogaz.site.paymentService.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.validation.constraint.SogazDomain
import java.net.URI
import javax.validation.constraints.NotNull

data class PayQueryParamsWithRequiredFields(
    @field:Schema(
        description = "Ссылка для редиректа после успешной оплаты",
        example = "https://example.com/success",
        format = "uri",
    )
    @field:SogazDomain
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturn: URI,
    @field:Schema(
        description = "Альтернативная ссылка для редиректа после успешной оплаты",
        example = "https://example.com/success-alt",
        format = "uri",
    )
    @field:SogazDomain
    @field:NotNull(message = "urlToReturnS is required")
    override val urlToReturnS: URI,
    @field:Schema(
        description = "Ссылка для редиректа при ошибке оплаты",
        example = "https://example.com/fail",
        format = "uri",
    )
    @field:SogazDomain
    @field:NotNull(message = "urlToReturnF is required")
    override val urlToReturnF: URI,
    @field:Schema(
        description = "Флаг анонимизированной оплаты",
        example = "false",
    )
    override val depersonalization: Boolean = false,
) : PayQueryParams()
