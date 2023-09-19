package io.primer.android.domain.error.models

import java.util.UUID

internal class NolPayError(errorCode: String, errorMessage: String?) : PrimerError() {

    override val errorId = "nol-pay-sdk-error"
    override val description = "Nol SDK encountered an error $errorCode. $errorMessage"
    override val diagnosticsId = UUID.randomUUID().toString()
    override val exposedError = this
    override val recoverySuggestion: String? = null
}
