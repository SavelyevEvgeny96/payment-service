package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.exceptionStarter.starter.dto.ValidationErrorData
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.validation.anatationsConstraints.ValidatePaymentRequest

/**
 * Объединенный валидатор для проверки всех полей в PaymentRequest.
 * Сначала проверяются все поля, затем ошибки собираются в одном месте,
 * и если есть ошибки, выбрасывается исключение.
 */
class PaymentRequestValidator : ConstraintValidator<ValidatePaymentRequest, PaymentRequest> {
    override fun initialize(constraintAnnotation: ValidatePaymentRequest?) {}

    override fun isValid(
        value: PaymentRequest?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) {
            throw BusinessException(CustomPaymentErrors.CODE_ERROR_REQUIRED_DATA)
        }

        val validationErrors = mutableListOf<ValidationErrorData>()

        val bankValidator = BankValidator()
        if (!bankValidator.isValid(value.bank)) {
            validationErrors.add(ValidationErrorData("bank", "Значение должно содержать gpb"))
        }

        val emailValidator = EmailValidator()
        if (!emailValidator.isValid(value.recipientEmail)) {
            validationErrors.add(
                ValidationErrorData("recipientEmail", "Значение должно содержать латинские буквы, цифры и специальные символы"),
            )
        }

        val emailValidatorManager = EmailValidator()
        if (!emailValidatorManager.isValid(value.managerEmail)) {
            validationErrors.add(
                ValidationErrorData("managerEmail", "Значение должно содержать латинские буквы, цифры и специальные символы"),
            )
        }

        val externalSystemCodeValidator = ExternalSystemCodeValidator()
        if (!externalSystemCodeValidator.isValid(value.externalSystemCode)) {
            validationErrors.add(ValidationErrorData("externalSystemCode", "Значение должно содержать ADI, FOP, LK, 1C"))
        }

        val paymentEndDateValidator = PaymentEndDateValidator()
        if (!paymentEndDateValidator.isValid(value.paymentEndDate)) {
            validationErrors.add(
                ValidationErrorData(
                    "paymentEndDate",
                    "Значение должно содержать только цифры, -, :, + и соответствовать маске yyyy-mm-ddThh:mm:ss+0000",
                ),
            )
        }

        val paymentPastDateValidator = NotPastDateValidator()
        if (!paymentPastDateValidator.isValid(value.paymentEndDate)) {
            validationErrors.add(ValidationErrorData("paymentEndDate", "Не может содержать дату в прошлом"))
        }

        val phoneValidator = PhoneValidator()
        if (!phoneValidator.isValid(value.recipientPhone)) {
            validationErrors.add(ValidationErrorData("recipientPhone", "Значение должно содержать только цифры, пробел, (, ), + и -"))
        }

        val policyholderValidator = PolicyholderValidator()
        if (!policyholderValidator.isValid(value.policyholder)) {
            validationErrors.add(ValidationErrorData("policyholder", "Значение должно содержать не менее 2 и не более 30 символов"))
        }

        val policyholderDocValidator = PolicyholderValidator()
        if (!policyholderDocValidator.isValidDoc(value.policyholderDoc)) {
            validationErrors.add(ValidationErrorData("policyholderDoc", "Значение должно содержать только цифры, пробел"))
        }

        val policyholderCorrectInput = PolicyholderValidator()
        if (!policyholderCorrectInput.isValidCorrectInput(value.policyholderDoc)) {
            validationErrors.add(ValidationErrorData("policyholder", "Значение должно содержать только русские буквы, пробел и тире"))
        }

        if (validationErrors.isNotEmpty()) {
            throw BusinessException(
                CustomPaymentErrors.CODE_ERROR_REQUIRED_DATA,
                validationErrors.toList(),
            )
        }

        return true
    }
}
