package ru.sogaz.site.paymentService.validation.constraint

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class SogazDomainValidatorTest {
    private lateinit var sogazDomainValidator: SogazDomainValidator

    @BeforeEach
    fun setup() {
        val regex = Regex("^(.*\\.sogaz\\.ru|lk\\.health-and-care\\.ru)(/[^ ]*)?\$")
        sogazDomainValidator = SogazDomainValidator(regex)
    }

    @Test
    fun `должен вернуть тру при валидации test sogaz ru`() {
        val ressult = sogazDomainValidator.isValid("test.sogaz.ru", null)
        assertTrue(ressult)
    }

    @Test
    fun `должен вернуть тру при валидации www sogaz ru`() {
        val ressult = sogazDomainValidator.isValid("www.sogaz.ru", null)
        assertTrue(ressult)
    }

    @Test
    fun `должен вернуть тру при валидации lk health-and-care ru`() {
        val ressult = sogazDomainValidator.isValid("lk.health-and-care.ru", null)
        assertTrue(ressult)
    }

    @Test
    fun `должен вернуть false при валидации test sogazz ru`() {
        val ressult = sogazDomainValidator.isValid("test.sogazz.ru", null)
        assertFalse(ressult)
    }

    @Test
    fun `должен вернуть false при валидации lkk health-and-care ru`() {
        val ressult = sogazDomainValidator.isValid("lkk.health-and-care.ru", null)
        assertFalse(ressult)
    }

    @Test
    fun `должен вернуть тру при валидации test sogaz ru test`() {
        val ressult = sogazDomainValidator.isValid("test.sogaz.ru/test", null)
        assertTrue(ressult)
    }

    @Test
    fun `должен вернуть тру при валидации lk health-and-care ru test`() {
        val ressult = sogazDomainValidator.isValid("lk.health-and-care.ru/test", null)
        assertTrue(ressult)
    }
}
