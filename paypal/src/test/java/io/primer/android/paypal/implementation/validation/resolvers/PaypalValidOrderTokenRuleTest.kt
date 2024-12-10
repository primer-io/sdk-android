package io.primer.android.paypal.implementation.validation.resolvers

import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.paypal.implementation.errors.data.exception.PaypalIllegalValueKey
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class PaypalValidOrderTokenRuleTest {

    private val rule = PaypalValidOrderTokenRule()

    @Test
    fun `validate should return success for non-null token`() {
        val token = "validToken"

        val result = rule.validate(token)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validate should return failure for null token`() {
        val token: String? = null

        val result = rule.validate(token)

        assertIs<ValidationResult.Failure>(result)
        assertTrue(result.exception is IllegalValueException)
        assertEquals(PaypalIllegalValueKey.INTENT_CHECKOUT_TOKEN, (result.exception as IllegalValueException).key)
    }
}
