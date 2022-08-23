package io.primer.android.components.domain.payments.validation.otp.blik

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator

internal class BlikOtpCodeValidator : PaymentInputTypeValidator<String> {
    override fun validate(input: String?): PrimerInputValidationError? {
        if (input.isNullOrBlank()) {
            return PrimerInputValidationError(
                "invalid-otp-code",
                "Otp code can not be blank.",
                PrimerInputElementType.PHONE_NUMBER
            )
        } else if (OTP_CODE_REGEX.matches(input).not()) {
            return PrimerInputValidationError(
                "invalid-otp-number",
                "Otp code is not valid.",
                PrimerInputElementType.PHONE_NUMBER
            )
        }
        return null
    }

    private companion object {
        val OTP_CODE_REGEX = Regex("^(\\d){6}$")
    }
}
