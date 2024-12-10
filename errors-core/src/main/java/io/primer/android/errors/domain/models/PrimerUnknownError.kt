package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

class PrimerUnknownError(val message: String) : PrimerError() {
    override val errorId: String = "unknown-error"
    override val description: String = "Something went wrong. Message $message."
    override val diagnosticsId: String = UUID.randomUUID().toString()
    override val errorCode: String? = null
    override val recoverySuggestion: String = "Contact Primer and provide us with diagnostics id $diagnosticsId."
    override val exposedError: PrimerError = this
}
