package io.primer.android.completion

interface ActionResumeHandler {
    fun handleClientToken(clientToken: String?)
    fun handleError(error: Error)
}

internal class DefaultActionResumeHandler(
    private val completion: (String?, Error?) -> Unit,
) : ActionResumeHandler {

    override fun handleClientToken(clientToken: String?) {
        completion(clientToken, null)
    }

    override fun handleError(error: Error) {
        completion(null, error)
    }
}
