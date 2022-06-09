package io.primer.android.components.domain.core.mapper

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.card.CardInputData
import io.primer.android.components.domain.core.models.otp.OtpInputData
import io.primer.android.data.configuration.models.PrimerPaymentMethodType

internal class PrimerHeadlessUniversalCheckoutPaymentMethodMapper {

    fun getPrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType: PrimerPaymentMethodType
    ): PrimerHeadlessUniversalCheckoutPaymentMethod {
        return when (paymentMethodType) {
            PrimerPaymentMethodType.ADYEN_BLIK -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                OtpInputData::class
            )
            PrimerPaymentMethodType.PAYMENT_CARD -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                CardInputData::class
            )
            else -> PrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)
        }
    }
}
