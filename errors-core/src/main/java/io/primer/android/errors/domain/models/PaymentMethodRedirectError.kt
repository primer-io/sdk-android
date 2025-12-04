package io.primer.android.errors.domain.models

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

data class PaymentMethodRedirectError(
    val paymentMethodType: String,
    val uri: String,
) : PrimerError() {
    override val exposedError = this

    override val context: BaseContextParams
        get() =
            ErrorContextParams(errorId, paymentMethodType)

    override val errorId: String
        get() = "failed-to-redirect"

    override val description: String
        get() = "Failed to redirect to $uri."

    override val errorCode: String? = null

    override val diagnosticsId = UUID.randomUUID().toString()

    override val recoverySuggestion: String?
        get() = null
}
