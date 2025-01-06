package io.primer.android.stripe.ach.implementation.validation.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class StripeIllegalValueKey(override val key: String) :
    IllegalValueKey {
    STRIPE_PUBLISHABLE_KEY("primerSettings.paymentMethodOptions.stripeOptions.publishableKey"),
    STRIPE_MANDATE_DATA("primerSettings.paymentMethodOptions.stripeOptions.mandateData"),
}
