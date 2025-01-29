package io.primer.android

import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.stripe.ach.implementation.errors.domain.model.StripeError

@Suppress("TooManyFunctions")
@JvmDefaultWithCompatibility
interface PrimerCheckoutListener {
    fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerResumeDecisionHandler,
    ) = Unit

    fun onResumeSuccess(
        resumeToken: String,
        decisionHandler: PrimerResumeDecisionHandler,
    ) = Unit

    fun onResumePending(additionalInfo: PrimerCheckoutAdditionalInfo) = Unit

    fun onAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) = Unit

    fun onBeforePaymentCreated(
        paymentMethodData: PrimerPaymentMethodData,
        decisionHandler: PrimerPaymentCreationDecisionHandler,
    ) {
        decisionHandler.continuePaymentCreation()
    }

    fun onCheckoutCompleted(checkoutData: PrimerCheckoutData)

    fun onBeforeClientSessionUpdated() = Unit

    fun onClientSessionUpdated(clientSession: PrimerClientSession) = Unit

    fun onFailed(
        error: PrimerError,
        errorHandler: PrimerErrorDecisionHandler?,
    ) {
        errorHandler?.showErrorMessage(error.takeIf { it is StripeError }?.description)
    }

    fun onFailed(
        error: PrimerError,
        checkoutData: PrimerCheckoutData?,
        errorHandler: PrimerErrorDecisionHandler?,
    ) {
        errorHandler?.showErrorMessage(error.takeIf { it is StripeError }?.description)
    }

    fun onDismissed() = Unit
}
