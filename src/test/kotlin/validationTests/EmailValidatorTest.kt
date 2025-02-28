package validationTests
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.EmailValidator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {
    private lateinit var emailValidator: EmailValidator

    @BeforeEach
    fun setUp() {
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        emailValidator = EmailValidator(emailRegex)
    }

    @Test
    fun `должен вернуть true для валидного email`() {
        val validEmail = "test@example.com"
        val result = emailValidator.isValid(validEmail)
        assertTrue(result, "Правильный email должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для email без домена`() {
        val invalidEmail = "test@.com"
        val result = emailValidator.isValid(invalidEmail)
        assertFalse(result, "Email без домена должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть false для email без символа @`() {
        val invalidEmail = "testexample.com"
        val result = emailValidator.isValid(invalidEmail)
        assertFalse(result, "Email без символа '@' должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть false для email с недопустимыми символами`() {
        val invalidEmail = "test@exam@ple.com"
        val result = emailValidator.isValid(invalidEmail)
        assertFalse(result, "Email с недопустимыми символами должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть true для email с заглавными буквами`() {
        val validEmail = "Test@Example.Com"
        val result = emailValidator.isValid(validEmail)
        assertTrue(result, "Email с заглавными буквами должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для значения null`() {
        val result = emailValidator.isValid(null)
        assertFalse(result, "Null значение  не должно пройти валидацию")
    }
}
