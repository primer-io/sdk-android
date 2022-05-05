package io.primer.android.completion

interface ResumeDecisionHandler {

    fun handleError(message: String?)

    fun handleSuccess()

    fun handleNewClientToken(clientToken: String)
}
