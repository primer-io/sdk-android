package io.primer.android.errors.domain.models

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

class SessionUpdateError(
    serverDiagnosticsId: String?,
    serverDescription: String?,
) : PrimerError() {
    override val errorId = "failed-to-update-session"
    override val description = "Failed to update session. $serverDescription"
    override val errorCode: String? = null
    override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
    override val exposedError = this
    override val recoverySuggestion =
        "Ensure that the payment method is configured correctly on the dashboard (https://dashboard.primer.io/)"
    override val context: BaseContextParams get() =
        ErrorContextParams(errorId)
}
