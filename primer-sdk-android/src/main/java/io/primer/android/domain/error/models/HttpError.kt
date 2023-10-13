package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class HttpError : PrimerError() {
    abstract val code: Int
    abstract override val diagnosticsId: String

    override val errorId = "server-error"
    override val recoverySuggestion = "Check the server's response to debug this error further."

    class HttpUnauthorizedError(
        override val code: Int,
        serverDiagnosticsId: String?
    ) : HttpError() {

        override val description = "Server error [$code]"
        override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
        override val exposedError = UnauthorizedError(diagnosticsId)
    }

    class HttpServerError(
        override val code: Int,
        serverDiagnosticsId: String?,
        apiError: String
    ) : HttpError() {

        override val description = "Server error [$code] Response: $apiError"
        override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
        override val exposedError = ServerError(apiError, diagnosticsId)
    }

    class HttpClientError(
        override val code: Int,
        serverDiagnosticsId: String?,
        apiError: String,
        override val exposedError: PrimerError
    ) : HttpError() {

        override val description = "Server error [$code] Response: $apiError"
        override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
    }
}
