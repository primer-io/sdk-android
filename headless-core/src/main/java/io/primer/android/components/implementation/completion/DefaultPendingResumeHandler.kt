package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler

internal class DefaultPendingResumeHandler(private val analyticsRepository: AnalyticsRepository) :
    PendingResumeHandler {
    override fun handle(additionalInfo: PrimerCheckoutAdditionalInfo) {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_PENDING
            )
        )
        PrimerHeadlessUniversalCheckout.instance.checkoutListener?.onResumePending(additionalInfo)
    }
}
