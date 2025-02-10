package validationTests

import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.NotPastDateValidator
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotPastDateValidatorTest {
    private val notPastDateValidator = NotPastDateValidator()

    @Test
    fun `должен вернуть true для даты, которая не в прошлом`() {
        val futureDate = ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
        val result = notPastDateValidator.isValid(futureDate)
        assertTrue(result, "Дата в будущем должна пройти валидацию")
    }

    @Test
    fun `должен вернуть false для даты, которая в прошлом`() {
        val pastDate = ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
        val result = notPastDateValidator.isValid(pastDate)
        assertFalse(result, "Дата в прошлом не должна пройти валидацию")
    }

    @Test
    fun `должен вернуть false для пустого значения`() {
        val result = notPastDateValidator.isValid("")
        assertFalse(result, "Пустое значение не должно пройти валидацию")
    }

    @Test
    fun `должен вернуть false для null значения`() {
        val result = notPastDateValidator.isValid(null)
        assertFalse(result, "Null значение не должно пройти валидацию")
    }
}
