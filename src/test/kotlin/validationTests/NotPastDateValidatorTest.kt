package validationTests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.validation.NotPastDateValidator
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NotPastDateValidatorTest {

    private val notPastDateValidator = NotPastDateValidator()

    @Test
    fun `должен вернуть true для даты, которая не в прошлом`() {
        // Тестируем, что дата в будущем проходит валидацию
        val futureDate =
            LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+0000"))
        val result = notPastDateValidator.isValid(futureDate)
        assertTrue(result, "Дата в будущем должна пройти валидацию")
    }

    @Test
    fun `должен вернуть false для даты, которая в прошлом`() {
        // Тестируем, что дата в прошлом не проходит валидацию
        val pastDate =
            LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+0000"))
        val result = notPastDateValidator.isValid(pastDate)
        assertFalse(result, "Дата в прошлом не должна пройти валидацию")
    }

    @Test
    fun `должен вернуть false для пустого значения`() {
        // Тестируем, что пустое значение не проходит валидацию
        val result = notPastDateValidator.isValid("")
        assertFalse(result, "Пустое значение не должно пройти валидацию")
    }

    @Test
    fun `должен вернуть false для null значения`() {
        // Тестируем, что null значение не проходит валидацию
        val result = notPastDateValidator.isValid(null)
        assertFalse(result, "Null значение не должно пройти валидацию")
    }
}
