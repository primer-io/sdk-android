package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.NolPayLinkCollectableData

internal class NolPayMobileNumberDataValidator : NolPayDataValidator<NolPayLinkCollectableData.NolPayPhoneData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayPhoneData): List<PrimerValidationError> {
//        return when {
//            t.mobileNumber.isBlank() -> {
//                return listOf(
//                    PrimerValidationError(
//                        INVALID_MOBILE_NUMBER_ERROR_ID,
//                        "Mobile number cannot be blank.",
//                    )
//                )
//            }
//            MOBILE_PHONE_REGEX.matches(t.mobileNumber).not() -> {
//                listOf(
//                    PrimerValidationError(
//                        INVALID_MOBILE_NUMBER_ERROR_ID,
//                        "Mobile number is not valid.",
//                    )
//                )
//            }
//            else -> emptyList()
//        }
        return emptyList()
    }

    private companion object {

        const val INVALID_MOBILE_NUMBER_ERROR_ID = "invalid-mobile-number"
        val MOBILE_PHONE_REGEX = Regex("^\\+(\\d){9}$")
    }
}
