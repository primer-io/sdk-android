package io.primer.cardShared.validation.domain

import io.primer.android.paymentmethods.PaymentInputTypeValidator
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.error.PrimerInputValidationError

class CardholderNameValidator : PaymentInputTypeValidator<String> {

    override suspend fun validate(input: String?): PrimerInputValidationError? {
        if (input.isNullOrBlank()) {
            return PrimerInputValidationError(
                "invalid-cardholder-name",
                "Cardholder name cannot be blank.",
                PrimerInputElementType.CARDHOLDER_NAME
            )
        }
        return null
    }
}
