package validationTests
import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.BankValidator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BankValidatorTest {
    private val bankValidator = BankValidator()

    @Test
    fun `должен вернуть true если значение 'gpb'`() {
        val result = bankValidator.isValid("gpb")
        assertTrue(result, "Значение должно быть допустимым, если оно равно 'gpb'")
    }

    @Test
    fun `должен вернуть false если значение не 'gpb'`() {
        val result = bankValidator.isValid("some_other_bank")
        assertFalse(result, "TЗначение должно быть недопустимым, если оно неравно 'gpb'")
    }

    @Test
    fun `должен вернуть true если значение null`() {
        val result = bankValidator.isValid(null)
        assertTrue(result, "Значение должно быть недопустимым, если оно равно null")
    }
    @Test
    fun `должен вернуть true если значение ""`() {
        val result = bankValidator.isValid("")
        assertTrue(result, "Значение должно быть недопустимым, если оно равно null")
    }
}
