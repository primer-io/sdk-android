package io.primer.android.domain.error.models

sealed class PrimerError {
    abstract val errorId: String
    abstract val description: String
    abstract val diagnosticsId: String
    internal abstract val exposedError: PrimerError
    abstract val recoverySuggestion: String?
}
