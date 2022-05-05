package io.primer.android.domain.error.models

internal class UnauthorizedError(
    override val diagnosticsId: String
) : PrimerError() {
    override val errorId = "unauthorized"
    override val description = "Failed to perform .... with the client token provided."
    override val recoverySuggestion = "Request a new client token and provide it on the SDK."
    override val exposedError = this
}
