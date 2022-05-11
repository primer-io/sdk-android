package io.primer.android.completion

interface PrimerResumeDecisionHandler {

    fun handleFailure(message: String?)

    fun handleSuccess()

    fun continueWithNewClientToken(clientToken: String)
}
