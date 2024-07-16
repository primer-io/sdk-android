package io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class StripeAchUserDetailsIllegalValueKey(
    override val key: String
) : IllegalValueKey {
    MISSING_FIRST_NAME("missing-first-name"),
    MISSING_LAST_NAME("missing-last-name"),
    MISSING_EMAIL_ADDRESS("missing-email-address")
}
