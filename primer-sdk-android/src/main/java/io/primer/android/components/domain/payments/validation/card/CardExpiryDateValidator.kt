package io.primer.android.components.domain.payments.validation.card

import androidx.core.text.isDigitsOnly
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator
import io.primer.android.ui.ExpiryDateFormatter

internal class CardExpiryDateValidator :
    PaymentInputTypeValidator<CardExpiryDateValidator.ExpiryData> {

    override fun validate(input: ExpiryData?): PrimerInputValidationError? {
        val month = input?.expirationMonth?.padStart(2, '0')
        val year = input?.expirationYear.orEmpty()
        return when {
            VALID_MONTH_PATTERN.toRegex().matches(month.orEmpty()).not() -> {
                PrimerInputValidationError(
                    "invalid-expiry-date",
                    "Expiry month is not valid",
                    PrimerInputElementType.EXPIRY_DATE
                )
            }
            year.isDigitsOnly().not() || year.length != VALID_YEAR_LENGTH -> {
                PrimerInputValidationError(
                    "invalid-expiry-date",
                    "Expiry year is not valid",
                    PrimerInputElementType.EXPIRY_DATE
                )
            }
            ExpiryDateFormatter.getDate(
                month?.toInt()?.minus(1),
                year.toInt()
            ) <= ExpiryDateFormatter.getDate() -> PrimerInputValidationError(
                "invalid-expiry-date",
                "Expiry date is not valid",
                PrimerInputElementType.EXPIRY_DATE
            )
            else -> null
        }
    }

    inner class ExpiryData(val expirationMonth: String, val expirationYear: String)

    private companion object {
        const val VALID_YEAR_LENGTH = 4
        const val VALID_MONTH_PATTERN = "0[1-9]|1[0-2]"
    }
}
