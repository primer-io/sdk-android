package io.primer.android

import io.primer.android.completion.CheckoutErrorHandler
import io.primer.android.completion.PaymentCreationDecisionHandler
import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.domain.CheckoutData
import io.primer.android.domain.action.models.ClientSession
import io.primer.android.domain.tokenization.models.PaymentMethodData
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.domain.error.models.PrimerError

interface CheckoutEventListener {

    fun onTokenizeSuccess(
        paymentMethodToken: PaymentMethodToken,
        decisionHandler: ResumeDecisionHandler
    ) = Unit

    fun onTokenAddedToVault(paymentMethodToken: PaymentMethodToken) = Unit

    fun onResume(
        resumeToken: String,
        decisionHandler: ResumeDecisionHandler
    ) = Unit

    fun onCheckoutDismissed(reason: CheckoutExitReason) = Unit

    fun onBeforePaymentCreated(
        paymentMethodData: PaymentMethodData,
        decisionHandler: PaymentCreationDecisionHandler
    ) {
        decisionHandler.continuePaymentCreation()
    }

    fun onCheckoutCompleted(checkoutData: CheckoutData)

    fun onBeforeClientSessionUpdated() = Unit

    fun onClientSessionUpdated(clientSession: ClientSession) = Unit

    fun onFailed(
        error: PrimerError,
        errorHandler: CheckoutErrorHandler?
    ) {
        errorHandler?.showErrorMessage(null)
    }

    fun onFailed(
        error: PrimerError,
        checkoutData: CheckoutData?,
        errorHandler: CheckoutErrorHandler?
    ) {
        errorHandler?.showErrorMessage(null)
    }
}
