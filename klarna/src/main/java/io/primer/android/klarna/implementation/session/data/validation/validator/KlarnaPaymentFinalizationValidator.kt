package io.primer.android.klarna.implementation.session.data.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError

internal object KlarnaPaymentFinalizationValidator {
    fun validate(isFinalizationRequired: Boolean): PrimerValidationError? =
        if (isFinalizationRequired) {
            null
        } else {
            PrimerValidationError(
                errorId = KlarnaValidations.PAYMENT_ALREADY_FINALIZED_ERROR_ID,
                description = "This payment was configured to finalized automatically.",
            )
        }
}
