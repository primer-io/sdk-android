package io.primer.android.components.domain.payments.validation.card

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator
import io.primer.android.ui.CardNumberFormatter

internal class CardNumberValidator : PaymentInputTypeValidator<String> {

    override fun validate(input: String?): PrimerInputValidationError? {
        val cardNumberFormatter =
            CardNumberFormatter.fromString(input.orEmpty(), replaceInvalid = false)
        return when {
            cardNumberFormatter.isEmpty() -> {
                PrimerInputValidationError(
                    "invalid-card-number",
                    "Card number can not be empty.",
                    PrimerInputElementType.CARD_NUMBER
                )
            }
            cardNumberFormatter.isValid().not() -> {
                PrimerInputValidationError(
                    "invalid-card-number",
                    "Card number is not valid",
                    PrimerInputElementType.CARD_NUMBER
                )
            }
            else -> null
        }
    }
}
