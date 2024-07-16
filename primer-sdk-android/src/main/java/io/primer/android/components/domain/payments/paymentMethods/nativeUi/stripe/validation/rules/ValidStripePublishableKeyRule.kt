package io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules

import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.exception.StripeIllegalValueKey
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.settings.PrimerStripeOptions

internal class ValidStripePublishableKeyRule : ValidationRule<PrimerStripeOptions> {
    override fun validate(t: PrimerStripeOptions): ValidationResult =
        when (t.publishableKey.isNullOrBlank()) {
            false -> ValidationResult.Success
            true -> ValidationResult.Failure(
                IllegalValueException(
                    key = StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY,
                    message = "Required value for " +
                        "${StripeIllegalValueKey.STRIPE_PUBLISHABLE_KEY.key} was null or blank."
                )
            )
        }
}
