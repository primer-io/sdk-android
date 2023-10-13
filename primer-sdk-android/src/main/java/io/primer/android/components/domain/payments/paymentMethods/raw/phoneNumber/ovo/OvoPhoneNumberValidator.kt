package io.primer.android.components.domain.payments.paymentMethods.raw.phoneNumber.ovo

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator

internal class OvoPhoneNumberValidator : PaymentInputTypeValidator<String> {
    override fun validate(input: String?): PrimerInputValidationError? {
        return if (input.isNullOrBlank()) {
            PrimerInputValidationError(
                "invalid-phone-number",
                "[invalid-phone-number] Phone number cannot be blank.",
                PrimerInputElementType.PHONE_NUMBER
            )
        } else if (PHONE_NUMBER_REGEX.matches(input).not()) {
            PrimerInputValidationError(
                "invalid-phone-number",
                "[invalid-phone-number] Phone number is not valid.",
                PrimerInputElementType.PHONE_NUMBER
            )
        } else { null }
    }

    private companion object {
        val PHONE_NUMBER_REGEX = Regex("^(\\+?628)(\\d{8,10})$")
    }
}
