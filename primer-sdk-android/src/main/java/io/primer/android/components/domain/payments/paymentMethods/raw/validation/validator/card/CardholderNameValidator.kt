package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator

internal class CardholderNameValidator : PaymentInputTypeValidator<String> {

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
