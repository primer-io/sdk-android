package io.primer.android.ipay88.implementation.validation.rules

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.InstantExecutorExtension
import io.primer.android.ipay88.implementation.errors.data.exception.IPay88IllegalValueKey
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ValidClientSessionCountryCodeRuleTest {
    private lateinit var clientSessionCountryCodeRule: ValidClientSessionCountryCodeRule

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clientSessionCountryCodeRule = ValidClientSessionCountryCodeRule()
    }

    @Test
    fun `validate() should return Success result when countryCode validation is successful`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.orderDetails?.countryCode }
            .returns(CountryCode.MY)
        every { iPay88ValidationData.clientToken.supportedCountryCode }
            .returns(CountryCode.MY.name)

        val validationResult = clientSessionCountryCodeRule.validate(iPay88ValidationData)
        assertEquals(ValidationResult.Success, validationResult)
    }

    @Test
    fun `validate() should return Failure result when countryCode validation failed`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.orderDetails?.countryCode }
            .returns(CountryCode.MY)
        every { iPay88ValidationData.clientToken.supportedCurrencyCode }
            .returns(CountryCode.AE.name)
        val validationResult = clientSessionCountryCodeRule.validate(iPay88ValidationData)
        val exception =
            (validationResult as ValidationResult.Failure).exception
                as IllegalClientSessionValueException
        assertEquals(
            IllegalClientSessionValueException::class,
            exception::class,
        )
        assertEquals(
            IPay88IllegalValueKey.ILLEGAL_COUNTRY_CODE,
            exception.key,
        )
    }
}
