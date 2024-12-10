package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal class ConnectivityError(message: String) : PrimerError() {

    override val errorId = "connectivity-errors"
    override val description = message
    override val errorCode: String? = null
    override val diagnosticsId = UUID.randomUUID().toString()
    override val exposedError = BadNetworkError(diagnosticsId)
    override val recoverySuggestion = "Please check underlying errors to investigate further."
}
