package ru.sogaz.site.paymentService.dto.data

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Список показов")
data class PayItemsSwaggerSchema(
    @field:Schema(
        description = "Наименование подзаказа",
        example = "SF-000011950 от 15.09.2026",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val param1: String,
    @field:Schema(
        description = "Наименование подзаказа",
        example = "SF-000011951 от 15.09.2026",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val param2: String,
)
