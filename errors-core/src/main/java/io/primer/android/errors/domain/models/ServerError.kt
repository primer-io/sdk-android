package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError

internal class ServerError(override val description: String, override val diagnosticsId: String) :
    PrimerError() {
    override val errorId = "server-error"
    override val errorCode: String? = null
    override val exposedError = this
    override val recoverySuggestion = "Please contact Primer with diagnostics id $diagnosticsId."
}
