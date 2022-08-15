package io.primer.android.components.domain.payments.validation.card

import androidx.core.text.isDigitsOnly
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator
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
                    "Card cvv can not be blank.",
                    PrimerInputElementType.CVV
                )
            }
            cvv.isDigitsOnly().not() || cvv.length != expectedCvvLength -> {
                PrimerInputValidationError(
                    "invalid-cvv",
                    "Card cvv is not valid.",
                    PrimerInputElementType.CVV
                )
            }
            else -> return null
        }
    }

    inner class CvvData(val cvv: String, val cardNumber: String)
}
