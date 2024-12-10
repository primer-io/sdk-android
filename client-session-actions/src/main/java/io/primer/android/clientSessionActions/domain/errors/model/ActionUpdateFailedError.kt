package io.primer.android.clientSessionActions.domain.errors.model

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal class ActionUpdateFailedError(
    serverDescription: String,
    serverDiagnosticsId: String?
) : PrimerError() {

    override val errorId = "failed-to-update-client-session"
    override val description = serverDescription
    override val errorCode: String? = null
    override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
    override val exposedError: PrimerError = this
    override val recoverySuggestion =
        "Contact Primer and provide us with diagnostics id $diagnosticsId"
}
