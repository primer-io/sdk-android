package io.primer.android.otp.implementation.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.otp.PrimerOtpData
import io.primer.android.paymentmethods.CollectableDataValidator

internal class OtpValidator : CollectableDataValidator<PrimerOtpData> {

    override suspend fun validate(t: PrimerOtpData) =
        Result.success(
            if (OTP_REGEX.matches(t.otp)) {
                emptyList()
            } else {
                listOf(
                    PrimerValidationError(
                        errorId = OtpValidations.INVALID_OTP_ERROR_ID,
                        description = "OTP should be six digits long."
                    )
                )
            }
        )

    private companion object {
        val OTP_REGEX = Regex("^(\\d){6}$")
    }
}
