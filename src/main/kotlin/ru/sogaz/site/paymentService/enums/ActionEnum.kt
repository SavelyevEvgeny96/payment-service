package ru.sogaz.site.paymentService.enums

enum class ActionEnum(
    val value: String,
) {
    RECEIPT_GENERATED_ACTION("Заказ оплачен"),
    RECEIPT_GENERATION_ERROR_ACTION("Ошибка при совершени платежа"),
    ;

    companion object {
        fun exists(value: String?): Boolean = ActionEnum.entries.any { it.value == value }
    }
}
