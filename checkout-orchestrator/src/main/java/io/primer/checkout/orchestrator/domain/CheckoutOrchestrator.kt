package io.primer.checkout.orchestrator.domain

import io.primer.executionengine.domain.models.Outcome

fun interface CheckoutOrchestrator {
    suspend fun start(
        paymentMethodType: String,
        payload: String,
    ): Result<Outcome>
}
