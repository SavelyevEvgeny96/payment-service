package ru.sogaz.site.paymentService.validation

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.ValidationException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.siter.models.resonses.ValidationErrorData

/**
 * Объединенный валидатор для проверки всех полей в PaymentRequest.
 * Сначала проверяются все поля, затем ошибки собираются в одном месте,
 * и если есть ошибки, выбрасывается исключение.
 */

class PaymentRequestValidator(
    private val paymentEndDateValidatorFormat: PaymentEndDateValidatorFormat,
    private val phoneValidator: PhoneValidator,
    private val emailValidator: EmailValidator,
    private val externalSystemCodeValidator: ExternalSystemCodeValidator,
) {
    private val logger = loggerFor(javaClass)

    fun isValid(
        paymentRequest: PaymentRequest?,
        paymentRequestWrapper: PaymentRequestWrapper,
    ) {
        logger.info("Начало валидации для traceId: ${paymentRequest?.traceId}")

        if (paymentRequest == null) {
            logger.error("Ошибка: PaymentRequest не был передан.")
            throw ValidationException(CustomPaymentErrors.CODE_ERROR_REQUIRED_DATA)
        }

        val validationErrors = CustomPaymentErrors.Companion.validationErrors
        val listResultError = mutableListOf<ValidationErrorData?>()

        val bankValidator = BankValidator()
        if (!bankValidator.isValid(paymentRequestWrapper.bank)) {
            listResultError.add(validationErrors[CustomPaymentErrors.BANK])
            logger.warn("Ошибка валидации для поля банка: ${paymentRequestWrapper.bank}")
        }

        if (!emailValidator.isValid(paymentRequestWrapper.recipientEmail)) {
            listResultError.add(validationErrors[CustomPaymentErrors.RECIPIENT_EMAIL])
            logger.warn("Ошибка валидации для email получателя: ${paymentRequestWrapper.recipientEmail}")
        }

        if (!emailValidator.isValid(paymentRequest.managerEmail)) {
            listResultError.add(validationErrors[CustomPaymentErrors.MANAGER_EMAIL])
            logger.warn("Ошибка валидации для email менеджера: ${paymentRequest.managerEmail}")
        }

        if (!externalSystemCodeValidator.isValid(paymentRequest.externalSystemCode)) {
            listResultError.add(validationErrors[CustomPaymentErrors.EXTERNAL_SYSTEM_CODE_VALIDATION])
            logger.warn("Ошибка валидации для внешнего кода системы: ${paymentRequest.externalSystemCode}")
        }

        if (!paymentEndDateValidatorFormat.isValid(paymentRequestWrapper.paymentEndDate)) {
            listResultError.add(validationErrors[CustomPaymentErrors.PAYMENT_END_DATE_MASK])
            logger.warn("Ошибка валидации для формата даты окончания платежа: ${paymentRequestWrapper.paymentEndDate}")
        }

        val paymentPastDateValidator = NotPastDateValidator()
        if (!paymentPastDateValidator.isValid(paymentRequestWrapper.paymentEndDate)) {
            listResultError.add(validationErrors[CustomPaymentErrors.PAYMENT_END_DATE])
            logger.warn("Ошибка: дата окончания платежа прошла для traceId: ${paymentRequest.traceId}")
        }

        if (!phoneValidator.isValid(paymentRequestWrapper.recipientPhone)) {
            listResultError.add(validationErrors[CustomPaymentErrors.RECIPIENT_PHONE_VALIDATION])
            logger.warn("Ошибка валидации для телефона получателя: ${paymentRequestWrapper.recipientPhone}")
        }

        val policyholderValidator = PolicyholderValidator()
        if (!policyholderValidator.isValid(paymentRequestWrapper.policyHolder)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER_LENGTH])
            logger.warn("Ошибка валидации для держателя полиса: ${paymentRequestWrapper.policyHolder}")
        }

        val policyholderDocValidator = PolicyholderValidator()
        if (!policyholderDocValidator.isValidDoc(paymentRequestWrapper.policyHolderDoc)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER_DOC])
            logger.warn("Ошибка валидации для документа держателя полиса: ${paymentRequestWrapper.policyHolderDoc}")
        }

        val policyholderCorrectInput = PolicyholderValidator()
        if (!policyholderCorrectInput.isValidCorrectInput(paymentRequestWrapper.policyHolder)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER])
            logger.warn("Ошибка валидации для корректности ввода полиса: ${paymentRequestWrapper.policyHolder}")
        }

        if (listResultError.isNotEmpty()) {
            logger.error(
                "Валидация не прошла для traceId: ${paymentRequest.traceId}." +
                    " Ошибки: ${listResultError.map { it?.error }}",
            )
            throw ValidationException(
                CustomPaymentErrors.CODE_ERROR_REQUIRED_DATA,
                paymentRequest.traceId,
                listResultError,
            )
        }

        logger.info("Валидация прошла успешно для traceId: ${paymentRequest.traceId}")
    }
}
