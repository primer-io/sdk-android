package io.primer.android.payments.core.create.domain.handler

import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

interface PostTokenizationHandler {

    suspend fun handle(paymentMethodTokenData: PrimerPaymentMethodTokenData): Result<PaymentDecision>
}
