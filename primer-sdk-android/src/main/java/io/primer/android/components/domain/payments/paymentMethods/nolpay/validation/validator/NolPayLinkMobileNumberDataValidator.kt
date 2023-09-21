package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData

internal class NolPayLinkMobileNumberDataValidator :
    NolPayDataValidator<NolPayLinkCollectableData.NolPayPhoneData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayPhoneData) = when {
        t.mobileNumber.isBlank() -> {
            listOf(
                PrimerValidationError(
                    INVALID_MOBILE_NUMBER_ERROR_ID,
                    "Mobile number cannot be blank.",
                )
            )
        }

        DIALLING_CODE_REGEX.matches(t.phoneCountryDiallingCode).not() -> {
            listOf(
                PrimerValidationError(
                    INVALID_DIALLING_CODE_ERROR_ID,
                    "Mobile number dialling code is not valid.",
                )
            )
        }

        MOBILE_PHONE_REGEX.matches(t.mobileNumber).not() -> {
            listOf(
                PrimerValidationError(
                    INVALID_MOBILE_NUMBER_ERROR_ID,
                    "Mobile number is not valid.",
                )
            )
        }

        else -> emptyList()
    }

    private companion object {

        const val INVALID_DIALLING_CODE_ERROR_ID = "invalid-phone-country-dialling-code"
        const val INVALID_MOBILE_NUMBER_ERROR_ID = "invalid-mobile-number"
        val DIALLING_CODE_REGEX = Regex("^\\+?\\d{1,3}(-\\d{1,4})?\$")
        val MOBILE_PHONE_REGEX = Regex("^\\d{7,15}\$")
    }
}
