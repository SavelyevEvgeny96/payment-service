package ru.sogaz.site.paymentService.validation

import ru.sogaz.site.paymentService.constants.ErrorMessages
import ru.sogaz.site.paymentService.exception.ValidationException

class PaymentValidator {

    fun validateEmail(email: String) {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$"
        if (!email.matches(Regex(emailRegex))) {
            throw ValidationException("recipientEmail", ErrorMessages.INVALID_EMAIL_FORMAT)
        }
    }

    fun validatePhone(phone: String) {
        val phoneRegex = "^[+\\d()\\s-]{10,15}\$"
        if (!phone.matches(Regex(phoneRegex))) {
            throw ValidationException("recipientPhone", ErrorMessages.INVALID_PHONE_FORMAT)
        }
    }

    fun validatePolicyholder(policyholder: String) {
        if (policyholder.length !in 2..30) {
            throw ValidationException("policyholder", ErrorMessages.POLICYHOLDER_NAME_LENGTH)
        }
    }

    fun validateExternalSystemCode(code: String) {
        val validCodes = listOf("ADI", "FOP", "LK", "1C")
        if (code !in validCodes) {
            throw ValidationException("externalSystemCode", ErrorMessages.INVALID_EXTERNAL_SYSTEM_CODE)
        }
    }

    fun validatePaymentEndDate(paymentEndDate: String) {
        val currentDate = System.currentTimeMillis()
        val paymentEndDateTime = paymentEndDate.toDateTime()
        if (paymentEndDateTime < currentDate) {
            throw ValidationException("paymentEndDate", ErrorMessages.PAYMENT_END_DATE_PAST)
        }
    }

    fun validateDateFormat(date: String) {
        val dateRegex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}"
        if (!date.matches(Regex(dateRegex))) {
            throw ValidationException("paymentEndDate", ErrorMessages.INVALID_DATE_FORMAT)
        }
    }

    fun validateBank(bank: String?) {
        if (bank != null && bank != "gpb") {
            throw ValidationException("bank", ErrorMessages.INVALID_BANK)
        }
    }

    private fun String.toDateTime(): Long {
// Преобразование строки в долгую дату
        return 0L
    }
}