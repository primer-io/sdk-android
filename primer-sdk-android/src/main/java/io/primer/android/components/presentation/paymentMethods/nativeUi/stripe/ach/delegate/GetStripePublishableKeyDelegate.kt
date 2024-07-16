package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.settings.PrimerSettings

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
