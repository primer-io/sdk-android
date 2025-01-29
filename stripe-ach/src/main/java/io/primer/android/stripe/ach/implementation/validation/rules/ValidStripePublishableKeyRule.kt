package io.primer.android.stripe.ach.implementation.validation.rules

import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.stripe.ach.implementation.validation.exception.StripeIllegalValueKey

internal class ValidStripePublishableKeyRule : ValidationRule<PrimerStripeOptions> {
    override fun validate(t: PrimerStripeOptions): ValidationResult =
        when (t.publishableKey.isNullOrBlank()) {
            false -> ValidationResult.Success
            true ->
                ValidationResult.Failure(
                    IllegalValueException(
                        key = StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY,
                        message =
                        "Required value for " +
                            "${StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY.key} was null or blank.",
                    ),
                )
        }
}
