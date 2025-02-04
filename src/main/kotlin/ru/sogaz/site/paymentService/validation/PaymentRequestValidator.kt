package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.ValidationException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.BANK
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_REQUIRED_DATA
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.EXTERNAL_SYSTEM_CODE_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.MANAGER_EMAIL
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.PAYMENT_END_DATE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.PAYMENT_END_DATE_MASK
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.POLICY_HOLDER
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.POLICY_HOLDER_DOC
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.POLICY_HOLDER_LENGTH
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.RECIPIENT_EMAIL
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.RECIPIENT_PHONE_VALIDATION
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.validation.anatationsConstraints.ValidatePaymentRequest
import ru.sogaz.siter.models.resonses.ValidationErrorData

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
            throw ValidationException(CODE_ERROR_REQUIRED_DATA)
        }

        val validationErrors = CustomPaymentErrors.Companion.validationErrors
        val listResultError = mutableListOf<ValidationErrorData?>()
        val bankValidator = BankValidator()
        if (!bankValidator.isValid(value.bank)) {
            listResultError.add(validationErrors[BANK])
        }

        val emailValidator = EmailValidator()
        if (!emailValidator.isValid(value.recipientEmail)) {
            listResultError.add(validationErrors[RECIPIENT_EMAIL])
        }

        val emailValidatorManager = EmailValidator()
        if (!emailValidatorManager.isValid(value.managerEmail)) {
            listResultError.add(validationErrors[MANAGER_EMAIL])
        }

        val externalSystemCodeValidator = ExternalSystemCodeValidator()
        if (!externalSystemCodeValidator.isValid(value.externalSystemCode)) {
            listResultError.add(validationErrors[EXTERNAL_SYSTEM_CODE_VALIDATION])
        }

        val paymentEndDateValidatorFormat = PaymentEndDateValidatorFormat()
        if (!paymentEndDateValidatorFormat.isValid(value.paymentEndDate)) {
            listResultError.add(validationErrors[PAYMENT_END_DATE_MASK])
        }

        val paymentPastDateValidator = NotPastDateValidator()
        if (!paymentPastDateValidator.isValid(value.paymentEndDate)) {
            listResultError.add(validationErrors[PAYMENT_END_DATE])        }

        val phoneValidator = PhoneValidator()
        if (!phoneValidator.isValid(value.recipientPhone)) {
            listResultError.add(validationErrors[RECIPIENT_PHONE_VALIDATION])
        }

        val policyholderValidator = PolicyholderValidator()
        if (!policyholderValidator.isValid(value.policyholder)) {
            listResultError.add(validationErrors[POLICY_HOLDER_LENGTH])
        }

        val policyholderDocValidator = PolicyholderValidator()
        if (!policyholderDocValidator.isValidDoc(value.policyholderDoc)) {
            listResultError.add(validationErrors[POLICY_HOLDER_DOC])
        }

        val policyholderCorrectInput = PolicyholderValidator()
        if (!policyholderCorrectInput.isValidCorrectInput(value.policyholderDoc)) {
            listResultError.add(validationErrors[POLICY_HOLDER])
        }

        if (validationErrors.isNotEmpty()) {
            throw ValidationException(
                CODE_ERROR_REQUIRED_DATA,
                null,
                validationErrors.values.toList()
            )
        }
        return true
    }
}
