package io.primer.android.domain.error.models

import io.primer.android.analytics.domain.models.BaseContextParams

sealed class PrimerError {
    abstract val errorId: String
    abstract val description: String
    abstract val diagnosticsId: String
    abstract val errorCode: String?
    abstract val recoverySuggestion: String?
    internal abstract val exposedError: PrimerError
    internal open val context: BaseContextParams? = null
}
