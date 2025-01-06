package io.primer.android.payments.core.tokenization.domain.handler

import io.primer.android.PrimerSessionIntent

interface PreTokenizationHandler {
    suspend fun handle(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent = PrimerSessionIntent.CHECKOUT,
    ): Result<Unit>
}
