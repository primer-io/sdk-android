package io.primer.android.domain.error.models

import io.primer.android.model.dto.PaymentMethodType
import java.util.UUID

internal class SessionCreateError(
    paymentMethodType: PaymentMethodType,
    serverDiagnosticsId: String?
) : PrimerError() {
    override val errorId = "failed-to-create-session"
    override val description = "Failed to create session for $paymentMethodType"
    override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
    override val exposedError = this
    override val recoverySuggestion =
        "Ensure that the $paymentMethodType is configured correctly " +
            "on the dashboard (https://dashboard.primer.io/)"
}
