package io.primer.checkout.orchestrator.domain

import io.primer.checkout.orchestrator.domain.model.CheckoutFlowOutcome
import io.primer.statetransport.domain.model.InstructionFetch

fun interface CheckoutOrchestrator {
    /**
     * Runs the orchestrated flow of [envelope], whose instruction must be an
     * [io.primer.statetransport.domain.model.ClientInstructions.Execute].
     */
    suspend fun start(
        paymentMethodType: String,
        envelope: InstructionFetch,
    ): Result<CheckoutFlowOutcome>
}
