package io.primer.android.components.domain.payments.paymentMethods.raw.card

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator
import io.primer.android.ui.CardNumberFormatter

internal class CardCvvValidator : PaymentInputTypeValidator<CardCvvValidator.CvvData> {

    override fun validate(input: CvvData?): PrimerInputValidationError? {
        val expectedCvvLength =
            CardNumberFormatter.fromString(input?.cardNumber.orEmpty(), replaceInvalid = false)
                .getCvvLength()
        val cvv = input?.cvv.orEmpty()

        return when {
            cvv.isBlank() -> {
                PrimerInputValidationError(
                    "invalid-cvv",
                    "[invalid-cvv] Card cvv cannot be blank.",
                    PrimerInputElementType.CVV
                )
            }
            DIGITS_ONLY_REGEX.matches(cvv).not() || cvv.length != expectedCvvLength -> {
                PrimerInputValidationError(
                    "invalid-cvv",
                    "[invalid-cvv] Card cvv is not valid.",
                    PrimerInputElementType.CVV
                )
            }
            else -> return null
        }
    }

    inner class CvvData(val cvv: String, val cardNumber: String)

    private companion object {
        val DIGITS_ONLY_REGEX = "^\\d+\$".toRegex()
    }
}
