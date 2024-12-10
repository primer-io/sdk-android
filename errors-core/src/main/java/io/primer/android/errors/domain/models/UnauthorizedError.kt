package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError

internal class UnauthorizedError(
    override val diagnosticsId: String
) : PrimerError() {
    override val errorId = "unauthorized"
    override val description = "Failed to perform .... with the client token provided."
    override val errorCode: String? = null
    override val recoverySuggestion = "Request a new client token and provide it on the SDK."
    override val exposedError = this
}
