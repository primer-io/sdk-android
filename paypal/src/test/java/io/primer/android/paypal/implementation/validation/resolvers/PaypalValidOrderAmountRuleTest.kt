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

internal class PaypalValidOrderAmountRuleTest {
    private val rule = PaypalValidOrderAmountRule()

    @Test
    fun `validate should return success for valid amount`() {
        val params =
            mockk<PaypalCreateOrderParams> {
                every { amount } returns 100
            }

        val result = rule.validate(params)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validate should return failure for invalid amount`() {
        val params =
            mockk<PaypalCreateOrderParams> {
                every { amount } returns 0
            }

        val result = rule.validate(params)

        assertIs<ValidationResult.Failure>(result)
        assertTrue(result.exception is IllegalValueException)
        assertEquals(PaypalInvalidValueKey.ILLEGAL_AMOUNT, (result.exception as IllegalValueException).key)
    }

    @Test
    fun `validate should return failure for null amount`() {
        val params =
            mockk<PaypalCreateOrderParams> {
                every { amount } returns null
            }

        val result = rule.validate(params)

        assertIs<ValidationResult.Failure>(result)
        assertTrue(result.exception is IllegalValueException)
        assertEquals(PaypalInvalidValueKey.ILLEGAL_AMOUNT, (result.exception as IllegalValueException).key)
    }
}
