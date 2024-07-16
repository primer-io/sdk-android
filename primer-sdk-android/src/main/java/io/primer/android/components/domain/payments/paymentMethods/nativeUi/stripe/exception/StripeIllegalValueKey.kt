package io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class StripeIllegalValueKey(override val key: String) : IllegalValueKey {
    STRIPE_PUBLISHABLE_KEY("primerSettings.paymentMethodOptions.stripeOptions.publishableKey"),
    STRIPE_MANDATE_DATA("primerSettings.paymentMethodOptions.stripeOptions.mandateData")
}
