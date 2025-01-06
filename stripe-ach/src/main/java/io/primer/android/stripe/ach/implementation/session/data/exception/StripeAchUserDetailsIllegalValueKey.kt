package io.primer.android.stripe.ach.implementation.session.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class StripeAchUserDetailsIllegalValueKey(
    override val key: String,
) : IllegalValueKey {
    MISSING_FIRST_NAME("missing-first-name"),
    MISSING_LAST_NAME("missing-last-name"),
    MISSING_EMAIL_ADDRESS("missing-email-address"),
}
