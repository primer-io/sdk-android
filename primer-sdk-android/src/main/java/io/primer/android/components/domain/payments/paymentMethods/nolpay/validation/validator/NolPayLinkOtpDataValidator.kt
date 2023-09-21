package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData

internal class NolPayLinkOtpDataValidator :
    NolPayDataValidator<NolPayLinkCollectableData.NolPayOtpData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayOtpData):
        List<PrimerValidationError> {
        return when {
            t.otpCode.isBlank() -> {
                return listOf(
                    PrimerValidationError(
                        INVALID_OTP_CODE_ERROR_ID,
                        "OTP code cannot be blank.",
                    )
                )
            }

            OTP_CODE_REGEX.matches(t.otpCode).not() -> {
                listOf(
                    PrimerValidationError(
                        INVALID_OTP_CODE_ERROR_ID,
                        "OTP code is not valid.",
                    )
                )
            }

            else -> emptyList()
        }
    }

    private companion object {

        const val INVALID_OTP_CODE_ERROR_ID = "invalid-otp-code"
        val OTP_CODE_REGEX = Regex("^(\\d){6}$")
    }
}
