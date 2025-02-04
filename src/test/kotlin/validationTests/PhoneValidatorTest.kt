package validationTests

import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.PhoneValidator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneValidatorTest {
    private val validator = PhoneValidator()

    @Test
    fun `должен вернуть true для правильного формата телефона с кодом страны`() {
        val validPhone = "+79919919999"
        val result = validator.isValid(validPhone)
        assertTrue(result, "Телефон с кодом страны должен пройти валидацию")
    }

    @Test
    fun `должен вернуть true для правильного формата телефона без кода страны`() {
        val validPhone = "89999999999"
        val result = validator.isValid(validPhone)
        assertTrue(result, "Телефон без кода страны должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для телефона с недостаточным количеством цифр`() {
        val invalidPhone = "+79999985"
        val result = validator.isValid(invalidPhone)
        assertFalse(result, "Телефон с недостаточным количеством цифр не должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для телефона с неправильным символом`() {
        val invalidPhone = "+12-3456A7890"
        val result = validator.isValid(invalidPhone)
        assertFalse(result, "Телефон с неправильными символами не должен пройти валидацию")
    }

    @Test
    fun `должен вернуть false для пустого значения`() {
        val emptyPhone = ""
        val result = validator.isValid(emptyPhone)
        assertFalse(result, "Пустое значение не должно быть валидным")
    }

    @Test
    fun `должен вернуть false для null значения`() {
        val nullPhone: String? = null
        val result = validator.isValid(nullPhone)
        assertFalse(result, "Значение null не должно быть валидным")
    }
}
