package io.primer.cardShared.validation.domain

import androidx.annotation.VisibleForTesting
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.paymentmethods.PaymentInputTypeValidator
import io.primer.cardShared.ExpiryDateFormatter

class CardExpiryDateValidator : PaymentInputTypeValidator<String> {
    override suspend fun validate(input: String?): PrimerInputValidationError? {
        val paddedInput = input?.padStart(EXPIRY_DATE_LENGTH, '0').orEmpty()
        return when {
            input.isNullOrBlank() -> {
                PrimerInputValidationError(
                    EXPIRY_DATE_INVALID_ERROR_ID,
                    "Card expiry date cannot be blank.",
                    PrimerInputElementType.EXPIRY_DATE,
                )
            }

            VALID_EXPIRY_DATE_PATTERN.matches(paddedInput).not() -> {
                PrimerInputValidationError(
                    EXPIRY_DATE_INVALID_ERROR_ID,
                    "Card expiry date is not valid. Valid expiry date format is MM/YYYY.",
                    PrimerInputElementType.EXPIRY_DATE,
                )
            }

            ExpiryDateFormatter.fromString(paddedInput).isValid().not() ->
                PrimerInputValidationError(
                    EXPIRY_DATE_INVALID_ERROR_ID,
                    "Card expiry date is not valid. ",
                    PrimerInputElementType.EXPIRY_DATE,
                )

            else -> null
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal companion object {
        const val EXPIRY_DATE_LENGTH = 7
        const val EXPIRY_DATE_INVALID_ERROR_ID = "invalid-expiry-date"
        val VALID_EXPIRY_DATE_PATTERN = "(0[1-9]|1[0-2])/(\\d{4})\$".toRegex()
    }
}
