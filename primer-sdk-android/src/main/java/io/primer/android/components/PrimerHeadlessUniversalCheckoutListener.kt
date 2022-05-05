package io.primer.android.components

import io.primer.android.completion.PaymentCreationDecisionHandler
import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.CheckoutData
import io.primer.android.domain.action.models.ClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PaymentMethodData
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.PrimerPaymentMethodType

interface PrimerHeadlessUniversalCheckoutListener {
    fun onClientSessionSetupSuccessfully(
        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    ) = Unit

    fun onTokenizationPreparation() = Unit
    fun onTokenizationStarted(paymentMethodType: PrimerPaymentMethodType) = Unit
    fun onPaymentMethodShowed() = Unit
    fun onTokenizeSuccess(
        paymentMethodToken: PaymentMethodToken,
        resumeHandler: ResumeDecisionHandler
    ) = Unit

    fun onResume(resumeToken: String, resumeHandler: ResumeDecisionHandler) = Unit

    fun onBeforePaymentCreated(
        data: PaymentMethodData,
        createPaymentHandler: PaymentCreationDecisionHandler
    ) {
        createPaymentHandler.continuePaymentCreation()
    }

    fun onCheckoutCompleted(checkoutData: CheckoutData)

    fun onBeforeClientSessionUpdated() = Unit

    fun onClientSessionUpdated(clientSession: ClientSession) = Unit

    fun onFailed(error: PrimerError) = Unit

    fun onFailed(error: PrimerError, checkoutData: CheckoutData? = null) = Unit
}
