package io.primer.android.domain.error.models

internal class ServerError(override val description: String, override val diagnosticsId: String) :
    PrimerError() {
    override val errorId = "server-error"
    override val exposedError = this
    override val recoverySuggestion = "Please contact Primer with diagnostics id $diagnosticsId."
}
