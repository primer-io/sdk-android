package io.primer.android.components.domain.payments.validation.card

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator

internal class CardholderNameValidator : PaymentInputTypeValidator<String> {

    override fun validate(input: String?): PrimerInputValidationError? {
        if (input?.trim()?.isBlank() == true) {
            return PrimerInputValidationError(
                "invalid-cardholder-name",
                "Cardholder name should not be blank",
                PrimerInputElementType.CARDHOLDER_NAME
            )
        }
        return null
    }
}
