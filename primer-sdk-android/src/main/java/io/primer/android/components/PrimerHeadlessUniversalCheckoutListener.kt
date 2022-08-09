package io.primer.android.components

import io.primer.android.ExperimentalPrimerApi
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

@JvmDefaultWithCompatibility
@ExperimentalPrimerApi
interface PrimerHeadlessUniversalCheckoutListener {
    fun onAvailablePaymentMethodsLoaded(
        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    )

    fun onPreparationStarted(paymentMethodType: String) = Unit
    fun onTokenizationStarted(paymentMethodType: String) = Unit
    fun onPaymentMethodShowed(paymentMethodType: String) = Unit
    fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerResumeDecisionHandler
    ) = Unit

    fun onResumeSuccess(resumeToken: String, decisionHandler: PrimerResumeDecisionHandler) = Unit

    fun onBeforePaymentCreated(
        paymentMethodData: PrimerPaymentMethodData,
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
