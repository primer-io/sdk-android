package io.primer.android.errors.domain.models

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

data class PaymentMethodCancelledError(
    val paymentMethodType: String
) : PrimerError() {

    override val exposedError = this

    override val context: BaseContextParams
        get() =
            ErrorContextParams(errorId, paymentMethodType)

    override val errorId: String
        get() = "payment-cancelled"

    override val description: String
        get() =
            "Vaulting/Checking out for $paymentMethodType was cancelled by the user."

    override val errorCode: String? = null

    override val diagnosticsId = UUID.randomUUID().toString()

    override val recoverySuggestion: String?
        get() = null
}
