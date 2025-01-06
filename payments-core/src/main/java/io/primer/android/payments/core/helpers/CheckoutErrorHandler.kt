package io.primer.android.payments.core.helpers

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Dispatches checkout failure via callbacks. Exposes a flow that emits whenever checkout fails.
 */
interface CheckoutErrorHandler {
    /**
     * Flow that emits whenever checkout fails.
     */
    val errors: Flow<PrimerError>

    /**
     * Dispatches checkout failure taking into consideration the payment handling type (auto or manual).
     */
    suspend fun handle(
        error: PrimerError,
        payment: Payment?,
    )
}
