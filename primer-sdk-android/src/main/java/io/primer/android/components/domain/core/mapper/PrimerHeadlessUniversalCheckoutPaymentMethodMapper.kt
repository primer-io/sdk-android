package io.primer.android.components.domain.core.mapper

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.otp.OtpInputData
import io.primer.android.data.configuration.models.PaymentMethodType

internal class PrimerHeadlessUniversalCheckoutPaymentMethodMapper {

    fun getPrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType: String
    ): PrimerHeadlessUniversalCheckoutPaymentMethod {
        return when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK.name -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                OtpInputData::class
            )
            PaymentMethodType.PAYMENT_CARD.name -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                PrimerRawCardData::class
            )
            else -> PrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)
        }
    }
}
