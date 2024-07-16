package io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.exception.StripeIllegalValueKey
import io.primer.android.data.base.exceptions.IllegalValueException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ValidStripePublishableKeyRuleTest {
    private val rule = ValidStripePublishableKeyRule()

    @Test
    fun `validate() should return success when the input is not blank`() {
        val result = rule.validate(mockk { every { publishableKey } returns "pk" })

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validate() should return failure when the input is blank`() {
        val result = rule.validate(mockk { every { publishableKey } returns "   " })

        assertEquals(
            ValidationResult.Failure(
                IllegalValueException(
                    key = StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY,
                    message = "Required value for " +
                        "${StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY.key} was null or blank."
                )
            ),
            result
        )
    }

    @Test
    fun `validate() should return failure when the input is null`() {
        val result = rule.validate(mockk { every { publishableKey } returns null })

        assertEquals(
            ValidationResult.Failure(
                IllegalValueException(
                    key = StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY,
                    message = "Required value for " +
                        "${StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY.key} was null or blank."
                )
            ),
            result
        )
    }
}
