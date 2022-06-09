package io.primer.android

import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

@JvmDefaultWithCompatibility
interface PrimerCheckoutListener {

    fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerResumeDecisionHandler
    ) = Unit

    fun onResumeSuccess(
        resumeToken: String,
        decisionHandler: PrimerResumeDecisionHandler
    ) = Unit

    fun onBeforePaymentCreated(
        paymentMethodData: PrimerPaymentMethodData,
        decisionHandler: PrimerPaymentCreationDecisionHandler
    ) {
        decisionHandler.continuePaymentCreation()
    }

    fun onCheckoutCompleted(checkoutData: PrimerCheckoutData)

    fun onBeforeClientSessionUpdated() = Unit

    fun onClientSessionUpdated(clientSession: PrimerClientSession) = Unit

    fun onFailed(
        error: PrimerError,
        errorHandler: PrimerErrorDecisionHandler?
    ) {
        errorHandler?.showErrorMessage(null)
    }

    fun onFailed(
        error: PrimerError,
        checkoutData: PrimerCheckoutData?,
        errorHandler: PrimerErrorDecisionHandler?
    ) {
        errorHandler?.showErrorMessage(null)
    }

    fun onDismissed() = Unit
}
