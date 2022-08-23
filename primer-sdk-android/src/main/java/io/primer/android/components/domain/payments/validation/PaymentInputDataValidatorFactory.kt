package io.primer.android.components.domain.payments.validation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeRawData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerRawPhoneNumberData
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.validation.card.CardInputDataValidator
import io.primer.android.components.domain.payments.validation.otp.blik.BlikInputDataValidator
import io.primer.android.components.domain.payments.validation.phoneNumber.ovo.OvoPhoneNumberInputDataValidator
import io.primer.android.data.configuration.models.PaymentMethodType

internal class PaymentInputDataValidatorFactory(
    private val checkoutModuleRepository: CheckoutModuleRepository,
) {

    fun getPaymentInputDataValidator(
        paymentMethodType: String,
        inputData: PrimerRawData
    ): PaymentInputDataValidator<PrimerRawData> {
        return when (inputData) {
            is PrimerRawCardData -> CardInputDataValidator(
                checkoutModuleRepository,
            )
            is PrimerRawPhoneNumberData -> {
                when (paymentMethodType) {
                    PaymentMethodType.XENDIT_OVO.name -> OvoPhoneNumberInputDataValidator()
                    else -> throw IllegalArgumentException(
                        "Unsupported phone data validation for $paymentMethodType."
                    )
                }
            }
            is PrimerOtpCodeRawData -> {
                when (paymentMethodType) {
                    PaymentMethodType.ADYEN_BLIK.name -> BlikInputDataValidator()
                    else -> throw IllegalArgumentException(
                        "Unsupported otp data validation for $paymentMethodType."
                    )
                }
            }
            else -> throw IllegalArgumentException(
                "Unsupported data validation for ${inputData::class}."
            )
        } as PaymentInputDataValidator<PrimerRawData>
    }
}
