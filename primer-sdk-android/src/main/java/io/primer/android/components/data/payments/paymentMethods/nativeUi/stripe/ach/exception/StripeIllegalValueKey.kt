package io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class StripeIllegalValueKey(override val key: String) : IllegalValueKey {
    MISSING_CLIENT_SECRET("missing-client-secret"),
    MISSING_PAYMENT_INTENT_ID("missing-payment-intent-id"),
    MISSING_COMPLETION_URL("missing-completion-url"),
    MISSING_PUBLISHABLE_KEY("missing-publishable-key"),
    MISSING_MANDATE_DATA("missing-mandate-data")
}
