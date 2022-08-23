package io.primer.android.components.domain.core.models.otp

import io.primer.android.components.domain.core.models.PrimerAsyncRawDataTokenizationHelper
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal data class PrimerOtpCodeRawData(val otpCode: String) : PrimerRawData {

    internal fun setTokenizableValues(
        descriptor: AsyncPaymentMethodDescriptor,
        redirectionUrl: String
    ):
        AsyncPaymentMethodDescriptor {
        return PrimerAsyncRawDataTokenizationHelper(redirectionUrl).setTokenizableData(descriptor)
            .apply {
                appendTokenizableValue("sessionInfo", "blikCode", otpCode)
            }
    }
}
