package io.primer.android.stripe.ach.implementation.session.data.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError

internal object FirstNameValidator {
    fun validate(value: String): PrimerValidationError? =
        if (value.isBlank()) {
            PrimerValidationError(
                errorId = StripeAchUserDetailsValidations.INVALID_CUSTOMER_FIRST_NAME_ERROR_ID,
                description = "The first name may not be blank.",
            )
        } else {
            null
        }
}
