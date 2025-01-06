package io.primer.android.paypal.implementation.validation.resolvers

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.paypal.implementation.errors.domain.exception.PaypalInvalidValueKey
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class PaypalValidOrderCurrencyRuleTest {
    private val rule = PaypalValidOrderCurrencyRule()

    @Test
    fun `validate should return success for valid currency code`() {
        val params =
            mockk<PaypalCreateOrderParams> {
                every { currencyCode } returns "USD"
            }

        val result = rule.validate(params)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validate should return failure for invalid currency code`() {
        val params =
            mockk<PaypalCreateOrderParams> {
                every { currencyCode } returns "dew43"
            }

        val result = rule.validate(params)

        assertIs<ValidationResult.Failure>(result)
        assertTrue(result.exception is IllegalValueException)
        assertEquals(PaypalInvalidValueKey.ILLEGAL_CURRENCY_CODE, (result.exception as IllegalValueException).key)
    }
}
