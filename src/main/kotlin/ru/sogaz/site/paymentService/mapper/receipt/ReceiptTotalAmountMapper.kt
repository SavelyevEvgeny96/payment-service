package ru.sogaz.site.paymentService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Named
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.service.payment.ReceiptServiceImpl.Companion.ERROR_FRACTION_SUM
import ru.sogaz.site.paymentService.service.payment.ReceiptServiceImpl.Companion.ERROR_HOLL_SUM
import ru.sogaz.site.paymentService.service.payment.ReceiptServiceImpl.Companion.ERROR_INCORRECT_SUM
import java.math.BigDecimal

@Mapper
abstract class ReceiptTotalAmountMapper {
    @Named("mapToBigDecimalAmount")
    fun mapToBigDecimalAmount(amount: String): BigDecimal =
        try {
            amount
                .replace(" ", "")
                .replace(",", ".")
                .toBigDecimal()
                .also(::checkAmount)
        } catch (ignore: NumberFormatException) {
            throw InnerException(getTraceId(), ERROR_INCORRECT_SUM + amount)
        }

    private fun checkAmount(amount: BigDecimal) {
        val parts = amount.toString().split(".")
        if (parts.size > 1 && parts[1].length > 2) {
            throw InnerException(getTraceId(), ERROR_FRACTION_SUM)
        }
        if (parts[0].length > 8) {
            throw InnerException(getTraceId(), ERROR_HOLL_SUM)
        }
    }
}
