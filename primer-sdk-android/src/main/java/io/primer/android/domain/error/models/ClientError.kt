package io.primer.android.domain.error.models

internal class ClientError(override val description: String, override val diagnosticsId: String) :
    PrimerError() {
    override val errorId = "client-error"
    override val exposedError = this
    override val recoverySuggestion = "Please contact Primer with diagnostics id $diagnosticsId."
}
