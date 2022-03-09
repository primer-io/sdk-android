package io.primer.android.components.domain.core.mapper

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.otp.OtpInputData
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerPaymentMethodType

internal class PrimerHeadlessUniversalCheckoutPaymentMethodMapper {

    fun getPrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType: PrimerPaymentMethodType
    ): PrimerHeadlessUniversalCheckoutPaymentMethod {
        return when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK -> PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType,
                OtpInputData::class
            )
            else -> PrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)
        }
    }
}
