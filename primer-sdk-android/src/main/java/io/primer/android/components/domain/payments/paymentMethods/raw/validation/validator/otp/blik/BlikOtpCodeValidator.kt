package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.otp.blik

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator

internal class BlikOtpCodeValidator : PaymentInputTypeValidator<String> {
    override suspend fun validate(input: String?): PrimerInputValidationError? {
        return when {
            input.isNullOrBlank() -> {
                return PrimerInputValidationError(
                    "invalid-otp-code",
                    "[invalid-otp-code] OTP code cannot be blank.",
                    PrimerInputElementType.OTP_CODE
                )
            }
            OTP_CODE_REGEX.matches(input).not() -> {
                PrimerInputValidationError(
                    "invalid-otp-code",
                    "[invalid-otp-code] OTP code is not valid.",
                    PrimerInputElementType.OTP_CODE
                )
            }
            else -> null
        }
    }

    private companion object {
        val OTP_CODE_REGEX = Regex("^(\\d){6}$")
    }
}
