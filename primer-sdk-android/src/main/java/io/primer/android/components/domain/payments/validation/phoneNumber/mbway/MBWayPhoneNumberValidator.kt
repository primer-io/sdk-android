package io.primer.android.components.domain.payments.validation.phoneNumber.mbway

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator

internal class MBWayPhoneNumberValidator : PaymentInputTypeValidator<String> {
    override fun validate(input: String?): PrimerInputValidationError? {
        return if (input.isNullOrBlank()) {
            PrimerInputValidationError(
                "invalid-phone-number",
                "Phone number can not be blank.",
                PrimerInputElementType.PHONE_NUMBER
            )
        } else if (PHONE_NUMBER_REGEX.matches(input).not()) {
            PrimerInputValidationError(
                "invalid-phone-number",
                "Phone number is not valid.",
                PrimerInputElementType.PHONE_NUMBER
            )
        } else null
    }

    internal companion object {
        val PHONE_NUMBER_REGEX = Regex("^(^\\+351|351)(\\d{8,10})$")
    }
}

internal fun CharSequence?.makePhoneDigitsOnly(): String? {
    return this?.replace(Regex("\\D"), "")
}
