package validationTests

import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.PolicyholderValidator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyholderValidatorTest {
    private val validator = PolicyholderValidator()

    // Тест для метода isValid
    @Test
    fun `должен вернуть true для строки длиной от 2 до 30 символов`() {
        val validName = "Иван Иванов"
        val result = validator.isValid(validName)
        assertTrue(result, "Имя должно содержать от 2 до 30 символов")
    }

    @Test
    fun `должен вернуть false для строки длиной меньше 2 символов`() {
        val shortName = "И"
        val result = validator.isValid(shortName)
        assertFalse(result, "Имя должно содержать минимум 2 символа")
    }

    @Test
    fun `должен вернуть false для строки длиной больше 30 символов`() {
        val longName = "Иван Иванов Иванов Иванов Иванов Иванов Иванов"
        val result = validator.isValid(longName)
        assertFalse(result, "Имя должно содержать максимум 30 символов")
    }

    // Тест для метода isValidDoc
    @Test
    fun `должен вернуть true для строки, содержащей только цифры и пробелы`() {
        val validDoc = "123 456 789"
        val result = validator.isValidDoc(validDoc)
        assertTrue(result, "Документ должен содержать только цифры и пробелы")
    }

    @Test
    fun `должен вернуть false для строки, содержащей другие символы`() {
        val invalidDoc = "123-456-789"
        val result = validator.isValidDoc(invalidDoc)
        assertFalse(result, "Документ не должен содержать символы, кроме цифр и пробелов")
    }

    // Тест для метода isValidCorrectInput
    @Test
    fun `должен вернуть true для строки, содержащей только русские буквы, пробелы и тире`() {
        val validName = "Иван Иванов"
        val result = validator.isValidCorrectInput(validName)
        assertTrue(result, "Имя должно содержать только русские буквы, пробелы и тире")
    }

    @Test
    fun `должен вернуть false для строки, содержащей другие  символы`() {
        val invalidName = "Иван_Иванов"
        val result = validator.isValidCorrectInput(invalidName)
        assertFalse(result, "Имя не должно содержать символы, кроме русских букв, пробелов и тире")
    }

    @Test
    fun `должен вернуть true для пустой строки`() {
        val emptyName = ""
        val result = validator.isValidCorrectInput(emptyName)
        assertTrue(result, "Имя не должно быть пустым")
    }
}
