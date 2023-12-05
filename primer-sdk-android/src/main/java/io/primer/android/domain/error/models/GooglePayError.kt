package io.primer.android.domain.error.models

import com.google.android.gms.common.api.Status
import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import java.util.UUID

internal sealed class GooglePayError : PrimerError() {

    class GooglePayInternalError(
        val status: Status
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

    override val errorCode: String? = null

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = null

    override val context: BaseContextParams get() =
        ErrorContextParams(errorId, PaymentMethodType.GOOGLE_PAY.name)
}
