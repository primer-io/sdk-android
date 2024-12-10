package io.primer.android.completion

interface PrimerPaymentCreationDecisionHandler {

    fun continuePaymentCreation()

    fun abortPaymentCreation(errorMessage: String?)
}
