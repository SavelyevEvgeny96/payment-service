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
    private val policyholderValidator: PolicyholderValidator,
) {
    companion object {
        const val VALUE_NOT_NULL_IS_EMPTY = "Значение не должно быть пустым или null"
    }

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
        if (paymentRequest.premiumAmount.isNullOrEmpty()) {
            listResultError.add(validationErrors[CustomPaymentErrors.PREMIUM_AMOUNT])
            logger.warn(VALUE_NOT_NULL_IS_EMPTY)
        }
        if (paymentRequest.operationId.isNullOrEmpty()) {
            listResultError.add(validationErrors[CustomPaymentErrors.OPERATION_ID])
            logger.warn(VALUE_NOT_NULL_IS_EMPTY)
        }
        if (paymentRequest.policyId.isNullOrEmpty()) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_ID])
            logger.warn(VALUE_NOT_NULL_IS_EMPTY)
        }
        if (paymentRequest.policyNumber.isNullOrEmpty()) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_NUMBER])
            logger.warn(VALUE_NOT_NULL_IS_EMPTY)
        }
        if (!emailValidator.isValid(paymentRequestWrapper.recipientEmail)) {
            listResultError.add(validationErrors[CustomPaymentErrors.RECIPIENT_EMAIL])
            logger.warn("Ошибка валидации для email получателя: ${paymentRequestWrapper.recipientEmail}")
        }

        if (!emailValidator.isValidManager(paymentRequest.managerEmail)) {
            listResultError.add(validationErrors[CustomPaymentErrors.MANAGER_EMAIL])
            logger.warn("Ошибка валидации для email менеджера: ${paymentRequest.managerEmail}")
        }

        if (!externalSystemCodeValidator.isValid(paymentRequest.externalSystemCode)) {
            listResultError.add(validationErrors[CustomPaymentErrors.EXTERNAL_SYSTEM_CODE_VALIDATION])
            logger.warn("Ошибка валидации для внешнего кода системы: ${paymentRequest.externalSystemCode}")
        }
        val paymentPastDateValidator = NotPastDateValidator()
        if (!paymentEndDateValidatorFormat.isValid(paymentRequestWrapper.paymentEndDate)) {
            listResultError.add(validationErrors[CustomPaymentErrors.PAYMENT_END_DATE_MASK])
            logger.warn("Ошибка валидации для формата даты окончания платежа: ${paymentRequestWrapper.paymentEndDate}")
        } else if (!paymentPastDateValidator.isValid(paymentRequestWrapper.paymentEndDate)) {
            listResultError.add(validationErrors[CustomPaymentErrors.PAYMENT_END_DATE])
            logger.warn("Ошибка: дата окончания платежа прошла для traceId: ${paymentRequest.traceId}")
        }

        if (!phoneValidator.isValid(paymentRequestWrapper.recipientPhone)) {
            listResultError.add(validationErrors[CustomPaymentErrors.RECIPIENT_PHONE_VALIDATION])
            logger.warn("Ошибка валидации для телефона получателя: ${paymentRequestWrapper.recipientPhone}")
        }

        if (!policyholderValidator.isValid(paymentRequestWrapper.policyHolder)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER_LENGTH])
            logger.warn("Ошибка валидации для держателя полиса: ${paymentRequestWrapper.policyHolder}")
        }

        if (!policyholderValidator.isValidDoc(paymentRequestWrapper.policyHolderDoc)) {
            listResultError.add(validationErrors[CustomPaymentErrors.POLICY_HOLDER_DOC])
            logger.warn("Ошибка валидации для документа держателя полиса: ${paymentRequestWrapper.policyHolderDoc}")
        }

        if (!policyholderValidator.isValidCorrectInput(paymentRequestWrapper.policyHolder)) {
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
