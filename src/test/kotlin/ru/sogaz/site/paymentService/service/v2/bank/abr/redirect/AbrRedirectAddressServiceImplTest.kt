package ru.sogaz.site.paymentService.service.v2.bank.abr.redirect

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import ru.sogaz.site.paymentService.model.v2.entity.redirect.RedirectAddresses
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.v2.redirect.RedirectAddressesRepository
import ru.sogaz.site.paymentService.service.v2.bank.abr.redirect.impl.AbrRedirectAddressServiceImpl
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AbrRedirectAddressServiceImplTest {
    @MockK
    private lateinit var redirectAddressesRepository: RedirectAddressesRepository

    @Test
    fun `createStateRedirectUrl should save redirect addresses and return ABR state URL`() {
        val redirectAddressesSlot = slot<RedirectAddresses>()
        every { redirectAddressesRepository.save(capture(redirectAddressesSlot)) } returns redirectAddresses()

        val result = service().createStateRedirectUrl(redirectParams())

        assertThat(result).isEqualTo("$TEST_ABR_STATE_REDIRECT_URL/$TEST_REDIRECT_ID")
        assertThat(redirectAddressesSlot.captured.urlToReturnS).isEqualTo(TEST_SUCCESS_URL)
        assertThat(redirectAddressesSlot.captured.urlToReturnF).isEqualTo(TEST_FAIL_URL)
        assertThat(redirectAddressesSlot.captured.urlToReturn).isEqualTo(TEST_RETURN_URL)
    }

    @Test
    fun `getRedirectView should redirect to success URL for FullyPaid status`() {
        every { redirectAddressesRepository.findById(TEST_REDIRECT_ID) } returns Optional.of(redirectAddresses())

        val result = service().getRedirectView(TEST_REDIRECT_ID.toString(), "FullyPaid")

        assertThat(result.url).isEqualTo(TEST_SUCCESS_URL)
        assertThat(result.statusCode).isEqualTo(HttpStatus.FOUND)
    }

    @Test
    fun `getRedirectView should redirect to failure URL for unsuccessful status`() {
        every { redirectAddressesRepository.findById(TEST_REDIRECT_ID) } returns Optional.of(redirectAddresses())

        val result = service().getRedirectView(TEST_REDIRECT_ID.toString(), "Declined")

        assertThat(result.url).isEqualTo(TEST_FAIL_URL)
    }

    @Test
    fun `getRedirectView should redirect to default URL when redirect addresses are not found`() {
        every { redirectAddressesRepository.findById(TEST_REDIRECT_ID) } returns Optional.empty()

        val result = service().getRedirectView(TEST_REDIRECT_ID.toString(), "FullyPaid")

        assertThat(result.url).isEqualTo(TEST_DEFAULT_URL)
    }

    @Test
    fun `getRedirectView should not call repository for incorrect redirect id`() {
        val result = service().getRedirectView("incorrect-id", "FullyPaid")

        assertThat(result.url).isEqualTo(TEST_DEFAULT_URL)
        verify(exactly = 0) { redirectAddressesRepository.findById(any()) }
    }

    private fun service() =
        AbrRedirectAddressServiceImpl(
            redirectAddressesRepository = redirectAddressesRepository,
            apiConfigProperties = ApiConfigProperties().apply {
                returnUrl = TEST_DEFAULT_URL
                abrStateRedirectUrl = TEST_ABR_STATE_REDIRECT_URL
            },
        )

    private fun redirectParams() =
        RedirectParams(
            urlToReturn = TEST_RETURN_URL,
            urlToReturnS = TEST_SUCCESS_URL,
            urlToReturnF = TEST_FAIL_URL,
        )

    private fun redirectAddresses() =
        RedirectAddresses(
            id = TEST_REDIRECT_ID,
            urlToReturnS = TEST_SUCCESS_URL,
            urlToReturnF = TEST_FAIL_URL,
            urlToReturn = TEST_RETURN_URL,
            createDate = null,
        )

    private companion object {
        val TEST_REDIRECT_ID: UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        const val TEST_ABR_STATE_REDIRECT_URL = "https://test-gateway-site-dmz.sogaz.ru/payment/abr/state"
        const val TEST_SUCCESS_URL = "https://www.sogaz.ru/success"
        const val TEST_FAIL_URL = "https://www.sogaz.ru/fail"
        const val TEST_RETURN_URL = "https://www.sogaz.ru/return"
        const val TEST_DEFAULT_URL = "https://www.sogaz.ru/"
    }
}
