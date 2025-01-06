package io.primer.android.data.payments.forms.datasource

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.primer.android.R
import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.data.payments.forms.models.helper.DialCodeCountryPrefix
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PhoneNumberLocalFormDataSourceTest {
    @MockK
    private lateinit var theme: PrimerTheme

    @MockK
    private lateinit var countriesRepository: CountriesRepository

    private lateinit var dataSource: PhoneNumberLocalFormDataSource

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `get() returns correct FormDataResponse for ADYEN_MBWAY in dark mode`() =
        runTest {
            every { theme.isDarkMode } returns true
            val phoneCode = mockk<PrimerPhoneCode>()
            every { countriesRepository.getPhoneCodeByCountryCode(CountryCode.PT) } returns phoneCode

            dataSource = PhoneNumberLocalFormDataSource(theme, countriesRepository, PaymentMethodType.ADYEN_MBWAY)

            val result = dataSource.get().first()

            assertNull(result.title)
            assertEquals(R.drawable.ic_logo_mbway_dark, result.logo)
            assertEquals(ButtonType.PAY, result.buttonType)
            assertNull(result.description)

            val input = result.inputs!!.single()
            assertEquals(FormType.PHONE, input.type)
            assertEquals("phoneNumber", input.id)
            assertEquals(R.string.input_hint_form_phone_number, input.hint)
            assertNull(input.level)
            assertNull(input.mask)
            assertNull(input.inputCharacters)
            assertEquals(18, input.maxInputLength)
            assertNull(input.validation)
            assertEquals(DialCodeCountryPrefix(phoneCode), (input.inputPrefix as DialCodeCountryPrefix))
        }

    @Test
    fun `get() returns correct FormDataResponse for ADYEN_MBWAY in light mode`() =
        runTest {
            every { theme.isDarkMode } returns false
            val phoneCode = mockk<PrimerPhoneCode>()
            every { countriesRepository.getPhoneCodeByCountryCode(CountryCode.PT) } returns phoneCode

            dataSource = PhoneNumberLocalFormDataSource(theme, countriesRepository, PaymentMethodType.ADYEN_MBWAY)

            val result = dataSource.get().first()

            assertNull(result.title)
            assertEquals(R.drawable.ic_logo_mbway_light, result.logo)
            assertEquals(ButtonType.PAY, result.buttonType)
            assertNull(result.description)

            val input = result.inputs!!.single()
            assertEquals(FormType.PHONE, input.type)
            assertEquals("phoneNumber", input.id)
            assertEquals(R.string.input_hint_form_phone_number, input.hint)
            assertNull(input.level)
            assertNull(input.mask)
            assertNull(input.inputCharacters)
            assertEquals(18, input.maxInputLength)
            assertNull(input.validation)
            assertEquals(DialCodeCountryPrefix(phoneCode), (input.inputPrefix as DialCodeCountryPrefix))
        }

    @Test
    fun `get() returns correct FormDataResponse for XENDIT_OVO in dark mode`() =
        runTest {
            every { theme.isDarkMode } returns true
            val phoneCode = mockk<PrimerPhoneCode>()
            every { countriesRepository.getPhoneCodeByCountryCode(CountryCode.ID) } returns phoneCode

            dataSource = PhoneNumberLocalFormDataSource(theme, countriesRepository, PaymentMethodType.XENDIT_OVO)

            val result = dataSource.get().first()

            assertNull(result.title)
            assertEquals(R.drawable.ic_logo_mbway_dark, result.logo)
            assertEquals(ButtonType.PAY, result.buttonType)
            assertNull(result.description)

            val input = result.inputs!!.single()
            assertEquals(FormType.PHONE, input.type)
            assertEquals("phoneNumber", input.id)
            assertEquals(R.string.input_hint_form_phone_number, input.hint)
            assertNull(input.level)
            assertNull(input.mask)
            assertNull(input.inputCharacters)
            assertEquals(18, input.maxInputLength)
            assertNull(input.validation)
            assertEquals(DialCodeCountryPrefix(phoneCode), (input.inputPrefix as DialCodeCountryPrefix))
        }

    @Test
    fun `get() returns correct FormDataResponse for XENDIT_OVO in light mode`() =
        runTest {
            every { theme.isDarkMode } returns false
            val phoneCode = mockk<PrimerPhoneCode>()
            every { countriesRepository.getPhoneCodeByCountryCode(CountryCode.ID) } returns phoneCode

            dataSource = PhoneNumberLocalFormDataSource(theme, countriesRepository, PaymentMethodType.XENDIT_OVO)

            val result = dataSource.get().first()

            assertNull(result.title)
            assertEquals(R.drawable.ic_logo_mbway_light, result.logo)
            assertEquals(ButtonType.PAY, result.buttonType)
            assertNull(result.description)

            val input = result.inputs!!.single()
            assertEquals(FormType.PHONE, input.type)
            assertEquals("phoneNumber", input.id)
            assertEquals(R.string.input_hint_form_phone_number, input.hint)
            assertNull(input.level)
            assertNull(input.mask)
            assertNull(input.inputCharacters)
            assertEquals(18, input.maxInputLength)
            assertNull(input.validation)
            assertEquals(DialCodeCountryPrefix(phoneCode), (input.inputPrefix as DialCodeCountryPrefix))
        }

    @Test
    fun `get() emits error for unsupported payment method type`() =
        runTest {
            every { theme.isDarkMode } returns true

            dataSource = PhoneNumberLocalFormDataSource(theme, countriesRepository, PaymentMethodType.UNKNOWN)

            assertThrows<IllegalStateException> {
                dataSource.get().first()
            }
        }
}
