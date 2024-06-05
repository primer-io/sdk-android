package io.primer.android.domain.error.models

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import java.util.UUID

internal class SessionCreateError(
    private val paymentMethodType: PaymentMethodType,
    serverDiagnosticsId: String?,
    serverDescription: String?
) : PrimerError() {
    override val errorId = "failed-to-create-session"
    override val description = "Failed to create session for $paymentMethodType. $serverDescription"
    override val errorCode: String? = null
    override val diagnosticsId = serverDiagnosticsId ?: UUID.randomUUID().toString()
    override val exposedError = this
    override val recoverySuggestion =
        "Ensure that the $paymentMethodType is configured correctly on the dashboard (https://dashboard.primer.io/)"
    override val context: BaseContextParams get() =
        ErrorContextParams(errorId, paymentMethodType.name)
}
