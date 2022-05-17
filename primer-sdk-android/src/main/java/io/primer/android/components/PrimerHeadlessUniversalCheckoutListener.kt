package io.primer.android.components

import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

@JvmDefaultWithCompatibility
interface PrimerHeadlessUniversalCheckoutListener {
    fun onClientSessionSetupSuccessfully(
        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    ) = Unit

    fun onTokenizationPreparation() = Unit
    fun onTokenizationStarted(paymentMethodType: PrimerPaymentMethodType) = Unit
    fun onPaymentMethodShowed() = Unit
    fun onTokenizeSuccess(
        paymentMethodToken: PrimerPaymentMethodTokenData,
        resumeHandler: PrimerResumeDecisionHandler
    ) = Unit

    fun onResumeSuccess(resumeToken: String, resumeHandler: PrimerResumeDecisionHandler) = Unit

    fun onBeforePaymentCreated(
        data: PrimerPaymentMethodData,
        createPaymentHandler: PrimerPaymentCreationDecisionHandler
    ) {
        createPaymentHandler.continuePaymentCreation()
    }

    fun onCheckoutCompleted(checkoutData: PrimerCheckoutData)

    fun onBeforeClientSessionUpdated() = Unit

    fun onClientSessionUpdated(clientSession: PrimerClientSession) = Unit

    fun onFailed(error: PrimerError) = Unit

    fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData? = null) = Unit
}
