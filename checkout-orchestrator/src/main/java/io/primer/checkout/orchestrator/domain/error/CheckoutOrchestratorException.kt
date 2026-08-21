package io.primer.checkout.orchestrator.domain.error

sealed class CheckoutOrchestratorException(message: String) : Exception(message) {
    class StateProcessorException(
        val code: String,
        val stateProcessorDiagnosticsId: String,
        val stateProcessorMessage: String,
    ) : CheckoutOrchestratorException(
        "State processor error [$code] (diagnosticsId=$stateProcessorDiagnosticsId): $stateProcessorMessage",
    )

    class TerminalErrorException : CheckoutOrchestratorException(
        "Checkout ended with an error outcome.",
    )
}
