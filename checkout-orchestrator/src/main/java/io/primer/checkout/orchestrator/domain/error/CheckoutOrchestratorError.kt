package io.primer.checkout.orchestrator.domain.error

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

sealed class CheckoutOrchestratorError : PrimerError() {

    data object CheckoutTerminalError : CheckoutOrchestratorError()

    data class StateProcessorError(
        val code: String,
        val stateProcessorDiagnosticsId: String,
        val stateProcessorMessage: String,
    ) : CheckoutOrchestratorError()

    override val errorId: String
        get() = when (this) {
            is CheckoutTerminalError -> "checkout-terminal-error"
            is StateProcessorError -> "state-processor-error"
        }

    override val description: String
        get() = when (this) {
            is CheckoutTerminalError -> "Checkout ended with an error outcome."
            is StateProcessorError ->
                "State processor error [$code] (diagnosticsId=$stateProcessorDiagnosticsId): $stateProcessorMessage"
        }

    override val errorCode: String? = null

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String? = null
}
