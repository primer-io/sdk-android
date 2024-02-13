package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator
import io.primer.android.ui.ExpiryDateFormatter
import java.util.Calendar

internal class CardExpiryDateValidator : PaymentInputTypeValidator<String> {

    override suspend fun validate(input: String?): PrimerInputValidationError? {
        val paddedInput = input?.padStart(EXPIRY_DATE_LENGTH, '0')
        return when {
            input.isNullOrBlank() -> {
                PrimerInputValidationError(
                    EXPIRY_DATE_INVALID_ERROR_ID,
                    "Card expiry date cannot be blank.",
                    PrimerInputElementType.EXPIRY_DATE
                )
            }
            VALID_EXPIRY_DATE_PATTERN.matches(paddedInput.orEmpty()).not() -> {
                PrimerInputValidationError(
                    EXPIRY_DATE_INVALID_ERROR_ID,
                    "Card expiry date is not valid. Valid expiry date format is MM/YYYY.",
                    PrimerInputElementType.EXPIRY_DATE
                )
            }
            VALID_EXPIRY_DATE_PATTERN.find(paddedInput.orEmpty())?.groups?.let { groups ->
                ExpiryDateFormatter.getDate(
                    groups[1]?.value?.toInt()?.minus(1),
                    groups[2]?.value?.toInt()
                ) <= ExpiryDateFormatter.getDate().apply {
                    add(Calendar.YEAR, -1)
                }
            } == true -> PrimerInputValidationError(
                EXPIRY_DATE_INVALID_ERROR_ID,
                "Card expiry date is not valid. " +
                    "Expiry date should not be less than a year in the past",
                PrimerInputElementType.EXPIRY_DATE
            )
            else -> null
        }
    }

    private companion object {
        const val EXPIRY_DATE_LENGTH = 7
        const val EXPIRY_DATE_INVALID_ERROR_ID = "invalid-expiry-date"
        val VALID_EXPIRY_DATE_PATTERN = "(0[1-9]|1[0-2])/(\\d{4})\$".toRegex()
    }
}
