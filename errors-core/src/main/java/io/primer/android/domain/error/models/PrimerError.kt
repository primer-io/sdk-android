// package structure is kept in order to maintain backward compatibility
package io.primer.android.domain.error.models

import io.primer.android.analytics.domain.models.BaseContextParams

abstract class PrimerError {
    abstract val errorId: String
    abstract val description: String
    abstract val diagnosticsId: String
    abstract val errorCode: String?
    abstract val recoverySuggestion: String?
    abstract val exposedError: PrimerError
    open val context: BaseContextParams? = null
}
