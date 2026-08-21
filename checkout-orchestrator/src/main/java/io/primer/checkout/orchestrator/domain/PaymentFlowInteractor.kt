package io.primer.checkout.orchestrator.domain

import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.Params
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.checkout.orchestrator.domain.model.CheckoutFlowOutcome
import io.primer.checkout.orchestrator.domain.model.PaymentFlowResult
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.InstructionFetch
import io.primer.statetransport.domain.repository.StateTransportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select
import kotlin.time.Duration.Companion.milliseconds

/**
 * Drives the backend-driven instruction loop: starts the payment, then keeps polling for
 * instructions while orchestrated flows (EXECUTE payloads) run concurrently, until the backend
 * serves an END. External cancellation propagates as [kotlinx.coroutines.CancellationException]
 * (teardown; no result).
 */
internal class PaymentFlowInteractor(
    private val repository: StateTransportRepository,
    private val configurationRepository: ConfigurationRepository,
    private val orchestrator: CheckoutOrchestrator,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<PaymentFlowResult, PaymentFlowInteractor.PaymentFlowParams>() {

    data class PaymentFlowParams(
        val paymentMethodType: String,
        val returnUri: String,
    ) : Params

    override suspend fun performAction(params: PaymentFlowParams): Result<PaymentFlowResult> =
        runSuspendCatching { runInstructionLoop(params) }

    private suspend fun runInstructionLoop(params: PaymentFlowParams): PaymentFlowResult =
        coroutineScope { pollUntilConcluded(params) }

    @Suppress("LoopWithTooManyJumpStatements")
    private suspend fun CoroutineScope.pollUntilConcluded(params: PaymentFlowParams): PaymentFlowResult {
        val clientSessionId = requireNotNull(
            configurationRepository.getConfiguration()
                .clientSession.clientSessionDataResponse.clientSessionId,
        )
        var envelope = repository.start(
            clientSessionId = clientSessionId,
            paymentMethodType = params.paymentMethodType,
            returnUri = params.returnUri,
        ).getOrThrow()
        var activeOrchestrator: Deferred<Result<CheckoutFlowOutcome>>? = null

        while (true) {
            val instruction = envelope.instruction
            if (instruction is ClientInstructions.End) {
                // The web SDK abandons the in-flight promise on END; with structured
                // concurrency the deferred must be cancelled explicitly so coroutineScope
                // can return.
                activeOrchestrator?.cancel()
                return PaymentFlowResult.Completed(instruction)
            }
            if (instruction is ClientInstructions.Execute && activeOrchestrator == null) {
                // NO DEDUP GUARD: the backend serves each EXECUTE once and answers WAIT until
                // it has an END or a NEW EXECUTE, so an incoming EXECUTE is always a new flow.
                activeOrchestrator = startOrchestrator(
                    paymentMethodType = params.paymentMethodType,
                    envelope = envelope,
                )
            }

            delay(instruction.pollDelayMillis().milliseconds)

            val poll = async { repository.fetchInstructions(clientSessionId) }
            val running = activeOrchestrator
            if (running == null) {
                envelope = poll.await().getOrThrow()
                continue
            }

            when (val winner = race(poll = poll, running = running)) {
                is RaceWinner.PollWon -> {
                    // Carry the running orchestrator into the next iteration; the
                    // delay-consumed poll is already awaited, so it is never restarted.
                    envelope = winner.fetch.getOrThrow()
                }

                is RaceWinner.OrchestratorFinished -> {
                    val outcome = winner.outcome.getOrElse { throwable ->
                        // Cancelling swallows the losing poll: the coroutine-native
                        // equivalent of catching the losing promise rejection.
                        poll.cancel()
                        throw throwable
                    }
                    when (outcome) {
                        CheckoutFlowOutcome.Cancelled -> {
                            poll.cancel()
                            return PaymentFlowResult.Cancelled
                        }

                        CheckoutFlowOutcome.Completed, CheckoutFlowOutcome.Pending -> {
                            // Keep polling: only the backend END concludes the checkout.
                            // Pending flows poll forever; there is NO client-side timeout.
                            activeOrchestrator = null
                            envelope = poll.await().getOrThrow()
                        }
                    }
                }
            }
        }
    }

    private fun CoroutineScope.startOrchestrator(
        paymentMethodType: String,
        envelope: InstructionFetch,
    ): Deferred<Result<CheckoutFlowOutcome>> = async {
        orchestrator.start(
            paymentMethodType = paymentMethodType,
            envelope = envelope,
        )
    }

    private suspend fun race(
        poll: Deferred<Result<InstructionFetch>>,
        running: Deferred<Result<CheckoutFlowOutcome>>,
    ): RaceWinner = select {
        poll.onAwait { RaceWinner.PollWon(it) }
        running.onAwait { RaceWinner.OrchestratorFinished(it) }
    }

    private sealed interface RaceWinner {
        data class PollWon(val fetch: Result<InstructionFetch>) : RaceWinner
        data class OrchestratorFinished(val outcome: Result<CheckoutFlowOutcome>) : RaceWinner
    }

    private fun ClientInstructions.pollDelayMillis(): Long = when (this) {
        is ClientInstructions.Execute -> pollDelayMilliseconds
        is ClientInstructions.Wait -> pollDelayMilliseconds
        is ClientInstructions.End -> error("END instructions never reach the poll delay.")
    }
}
