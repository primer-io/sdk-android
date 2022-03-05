package io.primer.android.components

import io.primer.android.completion.ResumeHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodToken

interface PrimerHeadlessUniversalCheckoutListener {
    fun onClientSessionSetupSuccessfully(
        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    ) = Unit

    fun onTokenizationPreparation() = Unit
    fun onTokenizationStarted() = Unit
    fun onPaymentMethodShowed() = Unit
    fun onTokenizationSuccess(
        paymentMethodToken: PaymentMethodToken,
        resumeHandler: ResumeHandler
    ) = Unit

    fun onResumeSuccess(resumeToken: String, resumeHandler: ResumeHandler) = Unit
    fun onError(error: APIError) = Unit
}
