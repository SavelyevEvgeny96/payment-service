package validationTests

import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.PaymentEndDateValidatorFormat
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaymentEndDateValidatorTest {
    private val validator = PaymentEndDateValidatorFormat()

    @Test
    fun `должен вернуть true для правильного формата даты`() {
        val validDate = "2025-02-04T23:36:44+03:00"
        val result = validator.isValid(validDate)
        assertTrue(result, "Дата должна быть в правильном формате")
    }

    @Test
    fun `должен вернуть false для неправильного формата даты`() {
        val invalidDate = "2025-02-04 23:36:44"
        val result = validator.isValid(invalidDate)
        assertFalse(result, "Дата не должна быть в неправильном формате")
    }

    @Test
    fun `должен вернуть false для пустого значения`() {
        val emptyDate = ""
        val result = validator.isValid(emptyDate)
        assertFalse(result, "Пустое значение не должно быть валидным")
    }

    @Test
    fun `должен вернуть false для null значения`() {
        val nullDate: String? = null
        val result = validator.isValid(nullDate)
        assertFalse(result, "Значение null не должно быть валидным")
    }
}
