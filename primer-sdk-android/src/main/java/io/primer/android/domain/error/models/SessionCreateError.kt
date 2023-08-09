package io.primer.android.domain.error.models

import io.primer.android.data.configuration.models.PaymentMethodType
import java.util.UUID

internal class SessionCreateError(
    paymentMethodType: PaymentMethodType,
    serverDiagnosticsId: String?,
    serverDescription: String?
) : PrimerError() {
    override val errorId = "failed-to-create-session"
    override val description = "Failed to create session for $paymentMethodType. $serverDescription"
    override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
    override val exposedError = this
    override val recoverySuggestion =
        "Ensure that the $paymentMethodType is configured correctly " +
            "on the dashboard (https://dashboard.primer.io/)"
}
