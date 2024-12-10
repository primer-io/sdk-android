package io.primer.android.clientToken.core.errors.domain.models

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal sealed class ClientTokenError : PrimerError() {

    data class InvalidClientTokenError(override val description: String) : ClientTokenError()

    data class ExpiredClientTokenError(override val description: String) : ClientTokenError()

    override val errorId: String
        get() = when (this) {
            is InvalidClientTokenError -> "invalid-client-token"
            is ExpiredClientTokenError -> "expired-client-token"
        }

    override val errorCode: String? = null

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is InvalidClientTokenError ->
                "Ensure that the client token fetched from your backend is a valid client token" +
                    " (i.e. not null, not blank, is valid JWT and it comes from Primer)."

            is ExpiredClientTokenError ->
                "Avoid storing client tokens locally." +
                    " Fetch a new client token to provide on when starting Primer."
        }
}
