package io.primer.checkout.orchestrator.domain.model

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment

sealed class CheckoutDecision(open val payment: Payment?) {
    data class Success(override val payment: Payment?) : CheckoutDecision(payment)
    data class Failure(val error: PrimerError, override val payment: Payment?) : CheckoutDecision(payment)
}
