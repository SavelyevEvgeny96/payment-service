package ru.sogaz.site.paymentService.validation

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.ValidationException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.siter.models.resonses.ValidationErrorData

/**
 * Объединенный валидатор для проверки всех полей в PaymentRequest.
 * Сначала проверяются все поля, затем ошибки собираются в одном месте,
 * и если есть ошибки, выбрасывается исключение.
 */
@Service
class PaymentRequestValidator {
    private val logger = loggerFor(javaClass)

    fun isValid(value: PaymentRequest?) {
        logger.info("Начало валидации для traceId: ${value?.traceId}")

        if (value == null) {
            logger.error("Ошибка: PaymentRequest не был передан.")
            throw ValidationException(CustomPaymentErrors.CODE_ERROR_REQUIRED_DATA)
        }

        val validationErrors = CustomPaymentErrors.Companion.validationErrors
        val listResultError = mutableListOf<ValidationErrorData?>()

        val bankValidator = BankValidator()
        if (!bankValidator.isValid(value.bank)) {
            listResultError.add(validationErrors[CustomPaymentErrors.BANK])
            logger.warn("Ошибка валидации для поля банка: ${value.bank}")
        }

        val emailValidator = EmailValidator()
        if (!emailValidator.isValid(value.recipientEmail)) {
            listResultError.add(validationErrors[CustomPaymentErrors.RECIPIENT_EMAIL])
            logger.warn("Ошибка валидации для email получателя: ${value.recipientEmail}")
        }

        val emailValidatorManager = EmailValidator()
        if (!emailValidatorManager.isValid(value.managerEmail)) {
            listResultError.add(validationErrors[CustomPaymentErrors.MANAGER_EMAIL])
            logger.warn("Ошибка валидации для email менеджера: ${value.managerEmail}")
        }

        val externalSystemCodeValidator = ExternalSystemCodeValidator()
        if (!externalSystemCodeValidator.isValid(value.externalSystemCode)) {
            listResultError.add(validationErrors[CustomPaymentErrors.EXTERNAL_SYSTEM_CODE_VALIDATION])
            logger.warn("Ошибка валидации для внешнего кода системы: ${value.externalSystemCode}")
        }

        val paymentEndDateValidatorFormat = PaymentEndDateValidatorFormat()
        if (!paymentEndDateValidatorFormat.isValid(value.paymentEndDate)) {
            listResultError.add(validationErrors[CustomPaymentErrors.PAYMENT_END_DATE_MASK])
            logger.warn("Ошибка валидации для формата даты окончания платежа: ${value.paymentEndDate}")
        }

        val paymentPastDateValidator = NotPastDateValidator()
        if (!paymentPastDateValidator.isValid(value.paymentEndDate)) {
            listResultError.add(validationErrors[CustomPaymentErrors.PAYMENT_END_DATE])
            logger.warn("Ошибка: дата окончания платежа прошла для traceId: ${value.traceId}")
        }

        val phoneValidator = PhoneValidator()
        if (!phoneValidator.isValid(value.recipientPhone)) {
            listResultError.add(validationErrors[CustomPaymentErrors.RECIPIENT_PHONE_VALIDATION])
            logger.warn("Ошибка валидации для телефона получателя: ${value.recipientPhone}")
        }

        val policyholderValidator = PolicyholderValidator()
        if (!policyholderValidator.isValid(value.policyHolder)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER_LENGTH])
            logger.warn("Ошибка валидации для держателя полиса: ${value.policyHolder}")
        }

        val policyholderDocValidator = PolicyholderValidator()
        if (!policyholderDocValidator.isValidDoc(value.policyHolderDoc)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER_DOC])
            logger.warn("Ошибка валидации для документа держателя полиса: ${value.policyHolderDoc}")
        }

        val policyholderCorrectInput = PolicyholderValidator()
        if (!policyholderCorrectInput.isValidCorrectInput(value.policyHolderDoc)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER])
            logger.warn("Ошибка валидации для корректности ввода полиса: ${value.policyHolderDoc}")
        }

        if (listResultError.isNotEmpty()) {
            logger.error(
                "Валидация не прошла для traceId: ${value.traceId}." +
                    " Ошибки: ${listResultError.map { it?.error }}",
            )
            throw ValidationException(
                CustomPaymentErrors.CODE_ERROR_REQUIRED_DATA,
                value.traceId,
                listResultError,
            )
        }

        logger.info("Валидация прошла успешно для traceId: ${value.traceId}")
    }
}
