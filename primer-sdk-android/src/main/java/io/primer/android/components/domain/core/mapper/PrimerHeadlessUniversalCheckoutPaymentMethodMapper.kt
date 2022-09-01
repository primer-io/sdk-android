package io.primer.android.components.domain.core.mapper

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeRawData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerRawPhoneNumberData
import io.primer.android.data.configuration.models.PaymentMethodType

internal class PrimerHeadlessUniversalCheckoutPaymentMethodMapper {

    fun getPrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType: String
    ): PrimerHeadlessUniversalCheckoutPaymentMethod {
        return when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK.name -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                PrimerOtpCodeRawData::class
            )
            PaymentMethodType.PAYMENT_CARD.name -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                PrimerRawCardData::class
            )
            PaymentMethodType.XENDIT_OVO.name -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                PrimerRawPhoneNumberData::class
            )
            PaymentMethodType.ADYEN_MBWAY.name -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                PrimerRawPhoneNumberData::class
            )
            else -> PrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)
        }
    }
}
