package io.primer.checkout.orchestrator.domain.model

/**
 * The result of a single orchestrated checkout flow (one EXECUTE instruction).
 */
sealed interface CheckoutFlowOutcome {

    /** The state processor reached a successful terminal outcome. */
    data object Completed : CheckoutFlowOutcome

    /** The state processor reached a cancelled terminal outcome. */
    data object Cancelled : CheckoutFlowOutcome

    /**
     * The state processor returned no action, terminal, or error; the flow awaits backend
     * progress and the instruction loop keeps polling until the backend END.
     */
    data object Pending : CheckoutFlowOutcome
}
