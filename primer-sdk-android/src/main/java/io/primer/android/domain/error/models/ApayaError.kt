package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class ApayaError : PrimerError() {

    class ApayaInternalError(
        val status: String?
    ) : ApayaError()

    override val errorId: String
        get() = when (this) {
            is ApayaInternalError -> "apaya-internal"
        }

    override val description: String
        get() = when (this) {
            is ApayaInternalError ->
                "Apaya internal error with status: $status (diagnosticsId: $diagnosticsId)"
        }

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = null
}
