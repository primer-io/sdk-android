package io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator

import androidx.core.util.PatternsCompat
import io.primer.android.components.domain.error.PrimerValidationError

internal object EmailAddressValidator {
    fun validate(emailAddress: String): PrimerValidationError? =
        if (!PatternsCompat.EMAIL_ADDRESS.toRegex().matches(emailAddress)) {
            PrimerValidationError(
                errorId = StripeAchUserDetailsValidations.INVALID_CUSTOMER_EMAIL_ADDRESS_ERROR_ID,
                description = "The email address is invalid."
            )
        } else {
            null
        }
}
