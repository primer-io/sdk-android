package io.primer.cardShared.validation.domain

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.paymentmethods.PaymentInputTypeValidator

private const val MIN = 2
private const val MAX = 45

class CardholderNameValidator : PaymentInputTypeValidator<String> {
    override suspend fun validate(input: String?): PrimerInputValidationError? =
        if (input.isNullOrBlank()) {
            PrimerInputValidationError(
                errorId = "invalid-cardholder-name",
                description = "Cardholder name cannot be blank.",
                inputElementType = PrimerInputElementType.CARDHOLDER_NAME,
            )
        } else if (input.length !in MIN..MAX) {
            PrimerInputValidationError(
                errorId = "invalid-cardholder-name",
                description = "Cardholder name must be between $MIN and $MAX characters.",
                inputElementType = PrimerInputElementType.CARDHOLDER_NAME,
            )
        } else {
            null
        }
}
