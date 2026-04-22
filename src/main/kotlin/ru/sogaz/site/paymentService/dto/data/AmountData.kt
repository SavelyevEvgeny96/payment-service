package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.enums.CurrencyEnum
import java.math.BigDecimal
import java.math.RoundingMode

data class AmountData(
    private val amount: BigDecimal,
    val currency: CurrencyEnum = CurrencyEnum.RUB,
) {
    fun getAmountInPennies(): Int = amount.movePointRight(2).intValueExact()

    fun getAmount(): Int = amount.setScale(0, RoundingMode.HALF_UP).toInt()
}
