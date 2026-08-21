package io.primer.checkout.orchestrator.data.mapper

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapper
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorError
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorException

internal class CheckoutOrchestratorErrorMapper : ErrorMapper {
    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is CheckoutOrchestratorException.StateProcessorException ->
                CheckoutOrchestratorError.StateProcessorError(
                    code = throwable.code,
                    stateProcessorDiagnosticsId = throwable.stateProcessorDiagnosticsId,
                    stateProcessorMessage = throwable.stateProcessorMessage,
                )

            is CheckoutOrchestratorException.TerminalErrorException ->
                CheckoutOrchestratorError.CheckoutTerminalError

            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
