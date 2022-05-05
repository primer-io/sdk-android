package io.primer.android.completion

interface PaymentCreationDecisionHandler {

    fun continuePaymentCreation()

    fun abortPaymentCreation(errorMessage: String?)
}
