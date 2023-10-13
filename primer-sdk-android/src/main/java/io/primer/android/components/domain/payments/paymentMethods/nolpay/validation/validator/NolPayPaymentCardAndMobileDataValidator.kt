package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.DIALLING_CODE_REGEX
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_DIALLING_CODE_ERROR_ID
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_MOBILE_NUMBER_ERROR_ID
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.MOBILE_PHONE_REGEX
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData

internal class NolPayPaymentCardAndMobileDataValidator :
    NolPayDataValidator<NolPayPaymentCollectableData.NolPayCardAndPhoneData> {
    override suspend fun validate(t: NolPayPaymentCollectableData.NolPayCardAndPhoneData):
        List<PrimerValidationError> {
        return when {
            t.nolPaymentCard.cardNumber.isBlank() -> {
                return listOf(
                    PrimerValidationError(
                        INVALID_CARD_NUMBER_ERROR_ID,
                        "Card number cannot be blank."
                    )
                )
            }

            t.mobileNumber.isBlank() -> {
                listOf(
                    PrimerValidationError(
                        INVALID_MOBILE_NUMBER_ERROR_ID,
                        "Mobile number cannot be blank."
                    )
                )
            }

            DIALLING_CODE_REGEX.matches(t.phoneCountryDiallingCode).not() -> {
                listOf(
                    PrimerValidationError(
                        INVALID_DIALLING_CODE_ERROR_ID,
                        "Mobile number dialling code is not valid."
                    )
                )
            }

            MOBILE_PHONE_REGEX.matches(t.mobileNumber).not() -> {
                listOf(
                    PrimerValidationError(
                        INVALID_MOBILE_NUMBER_ERROR_ID,
                        "Mobile number is not valid."
                    )
                )
            }

            else -> emptyList()
        }
    }
}
