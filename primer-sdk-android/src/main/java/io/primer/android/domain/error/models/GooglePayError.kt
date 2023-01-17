package io.primer.android.domain.error.models

import com.google.android.gms.common.api.Status
import java.util.UUID

internal sealed class GooglePayError : PrimerError() {

    class GooglePayInternalError(
        val status: Status,
    ) : GooglePayError()

    override val errorId: String
        get() = when (this) {
            is GooglePayInternalError -> "google-pay-internal"
        }

    override val description: String
        get() = when (this) {
            is GooglePayInternalError ->
                "Google pay internal error with status: $status (diagnosticsId: $diagnosticsId)"
        }

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = null
}
