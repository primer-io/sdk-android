package io.primer.android.payments.core.create.domain.model

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment

sealed class PaymentDecision(open val payment: Payment?) {
    data class Pending(val clientToken: String, override val payment: Payment?) :
        PaymentDecision(payment)

    data class Success(override val payment: Payment) : PaymentDecision(payment)

    data class Error(val error: PrimerError, override val payment: Payment) :
        PaymentDecision(payment)
}
