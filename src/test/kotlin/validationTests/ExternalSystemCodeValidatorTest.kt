package validationTests

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.ExternalSystemCodeValidator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalSystemCodeValidatorTest {
    private lateinit var externalSystemCodeValidator: ExternalSystemCodeValidator

    @BeforeEach
    fun setUp() {
        val externalSystemCodeRegex = Regex("^(ADI|FOP|LK|1C)$")
        externalSystemCodeValidator = ExternalSystemCodeValidator(externalSystemCodeRegex)
    }

    @Test
    fun `должен вернуть true для допустимого кода внешней системы ADI`() {
        val validCode = "ADI"
        val result = externalSystemCodeValidator.isValid(validCode)
        assertTrue(result, "Код внешней системы 'ADI' должен пройти валидацию")
    }

    @Test
    fun `должен вернуть true для допустимого кода внешней системы FOP`() {
        val validCode = "FOP"
        val result = externalSystemCodeValidator.isValid(validCode)
        assertTrue(result, "Код внешней системы 'FOP' должен пройти валидацию")
    }

    @Test
    fun `должен вернуть true для допустимого кода внешней системы LK`() {
        val validCode = "LK"
        val result = externalSystemCodeValidator.isValid(validCode)
        assertTrue(result, "Код внешней системы 'LK' должен пройти валидацию")
    }

    @Test
    fun `должен вернуть true для допустимого кода внешней системы 1C`() {
        val validCode = "1C"
        val result = externalSystemCodeValidator.isValid(validCode)
        assertTrue(result, "Код внешней системы '1C' должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для кода внешней системы, который не является допустимым`() {
        val invalidCode = "XYZ"
        val result = externalSystemCodeValidator.isValid(invalidCode)
        assertFalse(result, "Код внешней системы 'XYZ' должен не пройти валидацию")
    }

    @Test
    fun `должен вернуть false для null значения`() {
        val result = externalSystemCodeValidator.isValid(null)
        assertFalse(result, "Null значение должно не пройти валидацию")
    }
}
