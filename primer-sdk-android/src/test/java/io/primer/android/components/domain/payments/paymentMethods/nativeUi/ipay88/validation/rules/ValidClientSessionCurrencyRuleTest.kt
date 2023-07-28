package io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.exception.IPay88IllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.IPay88ValidationData
import io.primer.android.data.base.exceptions.IllegalClientSessionValueException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ValidClientSessionCurrencyRuleTest {

    private lateinit var clientSessionCurrencyRule: ValidClientSessionCurrencyRule

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clientSessionCurrencyRule = ValidClientSessionCurrencyRule()
    }

    @Test
    fun `validate() should return Success result when currency validation is successful`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.currencyCode }.returns("MY")
        every { iPay88ValidationData.clientToken.supportedCurrencyCode }.returns("MY")

        val validationResult = clientSessionCurrencyRule.validate(iPay88ValidationData)
        assertEquals(ValidationResult.Success, validationResult)
    }

    @Test
    fun `validate() should return Failure result when currency validation failed`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.currencyCode }.returns("MY")
        every { iPay88ValidationData.clientToken.supportedCurrencyCode }.returns("GB")
        val validationResult = clientSessionCurrencyRule.validate(iPay88ValidationData)
        val exception =
            (validationResult as ValidationResult.Failure).exception
                as IllegalClientSessionValueException
        assertEquals(
            IllegalClientSessionValueException::class,
            exception::class
        )
        assertEquals(
            IPay88IllegalValueKey.ILLEGAL_CURRENCY_CODE,
            exception.key
        )
    }
}
