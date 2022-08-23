package io.primer.android.components.domain.payments.validation.phoneNumber.ovo

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator

internal class OvoPhoneNumberValidator : PaymentInputTypeValidator<String> {
    override fun validate(input: String?): PrimerInputValidationError? {
        if (input.isNullOrBlank()) {
            return PrimerInputValidationError(
                "invalid-phone-number",
                "Phone number can not be blank.",
                PrimerInputElementType.PHONE_NUMBER
            )
        } else if (PHONE_NUMBER_REGEX.matches(input).not()) {
            return PrimerInputValidationError(
                "invalid-phone-number",
                "Phone number is not valid.",
                PrimerInputElementType.PHONE_NUMBER
            )
        }
        return null
    }

    private companion object {
        val PHONE_NUMBER_REGEX = Regex("^(^\\+628|628)(\\d{8,10})$")
    }
}
