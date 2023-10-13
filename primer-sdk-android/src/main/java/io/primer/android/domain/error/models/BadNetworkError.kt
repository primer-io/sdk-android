package io.primer.android.domain.error.models

internal class BadNetworkError(
    override val diagnosticsId: String
) : PrimerError() {
    override val errorId: String = "bad-network"
    override val description: String =
        "Failed to perform network request because internet connection is bad."
    override val exposedError: PrimerError = this
    override val recoverySuggestion: String = "Check the internet connection and retry."
}
