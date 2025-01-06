package io.primer.android.completion

interface PrimerResumeDecisionHandler : PrimerHeadlessUniversalCheckoutResumeDecisionHandler {
    fun handleFailure(message: String?)

    fun handleSuccess()
}
