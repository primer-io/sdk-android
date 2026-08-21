package io.primer.checkout.orchestrator.domain.model

import io.primer.statetransport.domain.model.ClientInstructions

/**
 * The conclusion of the instruction loop for a whole payment flow.
 */
internal sealed interface PaymentFlowResult {

    /** The backend served an END instruction: the checkout concluded. */
    data class Completed(val end: ClientInstructions.End) : PaymentFlowResult

    /** The running orchestrated flow was cancelled, aborting the whole payment flow. */
    data object Cancelled : PaymentFlowResult
}
