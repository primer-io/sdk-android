package io.primer.android.stripe.ach.implementation.session.data.validation.validator

import androidx.core.util.PatternsCompat
import io.primer.android.components.domain.error.PrimerValidationError

internal object EmailAddressValidator {
    fun validate(emailAddress: String): PrimerValidationError? =
        if (!PatternsCompat.EMAIL_ADDRESS.toRegex().matches(emailAddress)) {
            PrimerValidationError(
                errorId = StripeAchUserDetailsValidations.INVALID_CUSTOMER_EMAIL_ADDRESS_ERROR_ID,
                description = "The email address is invalid.",
            )
        } else {
            null
        }
}
