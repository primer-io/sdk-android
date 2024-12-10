package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError

internal class BadNetworkError(
    override val diagnosticsId: String
) : PrimerError() {
    override val errorId: String = "bad-network"
    override val description: String =
        "Failed to perform network request because internet connection is bad."
    override val errorCode: String? = null
    override val exposedError: PrimerError = this
    override val recoverySuggestion: String = "Check the internet connection and try again."
}
