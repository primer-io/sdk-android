package io.primer.android.stripe.ach.implementation.session.presentation

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.stripe.ach.implementation.session.data.exception.StripeIllegalValueKey

internal class GetStripePublishableKeyDelegate(
    private val primerSettings: PrimerSettings
) {
    operator fun invoke(): Result<String> = runCatching {
        requireNotNullCheck(
            primerSettings.paymentMethodOptions.stripeOptions.publishableKey,
            StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY
        )
    }
}
