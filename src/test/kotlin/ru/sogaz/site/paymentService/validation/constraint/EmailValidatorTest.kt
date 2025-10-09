package ru.sogaz.site.paymentService.validation.constraint
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.config.ValidatorConfig
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {
    private lateinit var emailValidator: EmailValidator

    @BeforeEach
    fun setUp() {
        emailValidator = ValidatorConfig().emailValidator()
    }

    @Test
    fun `должен вернуть true для валидного email`() {
        val validEmail = "test@example.com"
        val result = emailValidator.isValid(validEmail, null)
        assertTrue(result, "Правильный email должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для невалидного email`() {
        val validEmail = ".test@example.com"
        val result = emailValidator.isValid(validEmail, null)
        assertFalse(result, "Неправильный email точка в начале адрес или в конце или две точки подряд")
    }

    @Test
    fun `должен вернуть false для  невалидного email`() {
        val validEmail = "test@example.com."
        val result = emailValidator.isValid(validEmail, null)
        assertFalse(result, "Неправильный email точка в начале адрес или в конце или две точки подряд")
    }

    @Test
    fun `должен вернуть false  для  невалидного email`() {
        val validEmail = "test@example..com"
        val result = emailValidator.isValid(validEmail, null)
        assertFalse(result, "Неправильный email точка в начале адрес или в конце или две точки подряд")
    }

    @Test
    fun `должен вернуть false для email без домена`() {
        val invalidEmail = "test@.com"
        val result = emailValidator.isValid(invalidEmail, null)
        assertFalse(result, "Email без домена должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть false для email без символа @`() {
        val invalidEmail = "testexample.com"
        val result = emailValidator.isValid(invalidEmail, null)
        assertFalse(result, "Email без символа '@' должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть false для email с недопустимыми символами`() {
        val invalidEmail = "test@exam@ple.com"
        val result = emailValidator.isValid(invalidEmail, null)
        assertFalse(result, "Email с недопустимыми символами должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть true для email с заглавными буквами`() {
        val validEmail = "Test@Example.Com"
        val result = emailValidator.isValid(validEmail, null)
        assertTrue(result, "Email с заглавными буквами должен пройти валидацию")
    }
}
