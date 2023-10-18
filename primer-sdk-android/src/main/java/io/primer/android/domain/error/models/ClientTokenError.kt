package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class ClientTokenError : PrimerError() {

    object InvalidClientTokenError : ClientTokenError()

    object ExpiredClientTokenError : ClientTokenError()

    override val errorId: String
        get() = when (this) {
            is InvalidClientTokenError -> "invalid-client-token"
            is ExpiredClientTokenError -> "expired-client-token"
        }

    override val description: String
        get() = when (this) {
            is InvalidClientTokenError ->
                "Cannot initialize the SDK because the client token provided" +
                    " is not a valid client token."
            is ExpiredClientTokenError ->
                "Cannot initialize the SDK because the client token provided is expired."
        }

    override val errorCode: String? = null

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is InvalidClientTokenError ->
                "Ensure that the client token fetched from your backend is a valid client token" +
                    " (i.e. not null, not blank, and it comes from Primer)."
            is ExpiredClientTokenError ->
                "Avoid storing client tokens locally." +
                    " Fetch a new client token to provide on when starting Primer."
        }
}
