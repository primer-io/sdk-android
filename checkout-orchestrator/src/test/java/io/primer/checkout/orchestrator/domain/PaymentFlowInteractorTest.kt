@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.domain.model.ClientSession
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.checkout.orchestrator.domain.model.CheckoutFlowOutcome
import io.primer.checkout.orchestrator.domain.model.PaymentFlowResult
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.CurrentAttempt
import io.primer.statetransport.domain.model.InstructionFetch
import io.primer.statetransport.domain.repository.StateTransportRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class PaymentFlowInteractorTest {

    @MockK
    lateinit var repository: StateTransportRepository

    @MockK
    lateinit var configurationRepository: ConfigurationRepository

    @MockK
    lateinit var orchestrator: CheckoutOrchestrator

    private lateinit var interactor: PaymentFlowInteractor

    private val testDispatcher = StandardTestDispatcher()

    private val params = PaymentFlowInteractor.PaymentFlowParams(
        paymentMethodType = PAYMENT_METHOD_TYPE,
        returnUri = RETURN_URI,
    )

    private val end = ClientInstructions.End(checkoutOutcome = null, payment = null)
    private val execute = ClientInstructions.Execute(pollDelayMilliseconds = POLL_DELAY, payload = PAYLOAD)
    private val wait = ClientInstructions.Wait(pollDelayMilliseconds = POLL_DELAY)

    @BeforeEach
    fun setUp() {
        val clientSessionDataResponse = mockk<ClientSessionDataResponse>(relaxed = true) {
            every { clientSessionId } returns CLIENT_SESSION_ID
        }
        val configuration = mockk<Configuration>(relaxed = true) {
            every { clientSession } returns ClientSession(
                clientSessionDataResponse = clientSessionDataResponse,
            )
        }
        every { configurationRepository.getConfiguration() } returns configuration

        interactor = PaymentFlowInteractor(
            repository = repository,
            configurationRepository = configurationRepository,
            orchestrator = orchestrator,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `should complete without polling or orchestrator when pay returns End`() = runTest(testDispatcher) {
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(end))

        val result = interactor(params)

        assertEquals(PaymentFlowResult.Completed(end), result.getOrNull())
        coVerify(exactly = 1) { repository.start(CLIENT_SESSION_ID, PAYMENT_METHOD_TYPE, RETURN_URI) }
        coVerify(exactly = 0) { repository.fetchInstructions(any()) }
        coVerify(exactly = 0) { orchestrator.start(any(), any()) }
    }

    @Test
    fun `should keep polling while the orchestrator runs when pay returns Execute`() = runTest(testDispatcher) {
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
        val orchestratorGate = CompletableDeferred<Result<CheckoutFlowOutcome>>()
        coEvery { orchestrator.start(any(), any()) } coAnswers { orchestratorGate.await() }
        coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
            Result.success(InstructionFetch(wait)),
            Result.success(InstructionFetch(wait)),
            Result.success(InstructionFetch(end)),
        )

        val result = interactor(params)

        assertEquals(PaymentFlowResult.Completed(end), result.getOrNull())
        coVerify(exactly = 3) { repository.fetchInstructions(CLIENT_SESSION_ID) }
    }

    @Test
    fun `should not start a second orchestrator when a Wait arrives while the orchestrator runs`() =
        runTest(testDispatcher) {
            coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
            val orchestratorGate = CompletableDeferred<Result<CheckoutFlowOutcome>>()
            coEvery { orchestrator.start(any(), any()) } coAnswers { orchestratorGate.await() }
            coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
                Result.success(InstructionFetch(wait)),
                Result.success(InstructionFetch(end)),
            )

            val result = interactor(params)

            assertEquals(PaymentFlowResult.Completed(end), result.getOrNull())
            coVerify(exactly = 1) { orchestrator.start(any(), any()) }
        }

    @Test
    fun `should abort the flow and cancel the in-flight poll when the orchestrator returns Cancelled`() =
        runTest(testDispatcher) {
            coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
            val orchestratorGate = CompletableDeferred<Result<CheckoutFlowOutcome>>()
            coEvery { orchestrator.start(any(), any()) } coAnswers { orchestratorGate.await() }
            var pollCancelled = false
            coEvery { repository.fetchInstructions(any()) } coAnswers {
                orchestratorGate.complete(Result.success(CheckoutFlowOutcome.Cancelled))
                suspendCancellableCoroutine { continuation ->
                    continuation.invokeOnCancellation { pollCancelled = true }
                }
            }

            val result = interactor(params)

            assertEquals(PaymentFlowResult.Cancelled, result.getOrNull())
            assertTrue(pollCancelled)
        }

    @Test
    fun `should fail the flow and swallow the losing poll when the orchestrator fails`() = runTest(testDispatcher) {
        val exception = RuntimeException("orchestrated flow failed")
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
        val orchestratorGate = CompletableDeferred<Result<CheckoutFlowOutcome>>()
        coEvery { orchestrator.start(any(), any()) } coAnswers { orchestratorGate.await() }
        var pollCancelled = false
        coEvery { repository.fetchInstructions(any()) } coAnswers {
            orchestratorGate.complete(Result.failure(exception))
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation { pollCancelled = true }
            }
        }

        val result = interactor(params)

        // Coroutines copy exceptions crossing async boundaries for stack-trace recovery,
        // so assert on type and message instead of instance identity.
        val failure = result.exceptionOrNull()
        assertTrue(failure is RuntimeException)
        assertEquals(exception.message, failure?.message)
        assertTrue(pollCancelled)
    }

    @Test
    fun `should keep polling until the backend End when the orchestrator completes`() = runTest(testDispatcher) {
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
        coEvery {
            orchestrator.start(any(), any())
        } returns Result.success(CheckoutFlowOutcome.Completed)
        coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
            Result.success(InstructionFetch(wait)),
            Result.success(InstructionFetch(end)),
        )

        val result = interactor(params)

        assertEquals(PaymentFlowResult.Completed(end), result.getOrNull())
        coVerify(exactly = 2) { repository.fetchInstructions(CLIENT_SESSION_ID) }
    }

    @Test
    fun `should keep polling until the backend End when the orchestrator is pending`() = runTest(testDispatcher) {
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
        coEvery { orchestrator.start(any(), any()) } returns Result.success(CheckoutFlowOutcome.Pending)
        coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
            Result.success(InstructionFetch(wait)),
            Result.success(InstructionFetch(end)),
        )

        val result = interactor(params)

        assertEquals(PaymentFlowResult.Completed(end), result.getOrNull())
        coVerify(exactly = 2) { repository.fetchInstructions(CLIENT_SESSION_ID) }
        coVerify(exactly = 1) { orchestrator.start(any(), any()) }
    }

    @Test
    fun `should start a second orchestrator when a new Execute is served after a pending flow`() =
        runTest(testDispatcher) {
            val secondExecute = ClientInstructions.Execute(
                pollDelayMilliseconds = POLL_DELAY,
                payload = SECOND_PAYLOAD,
            )
            coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
            coEvery {
                orchestrator.start(any(), InstructionFetch(execute))
            } returns Result.success(CheckoutFlowOutcome.Pending)
            coEvery {
                orchestrator.start(any(), InstructionFetch(secondExecute))
            } returns Result.success(CheckoutFlowOutcome.Completed)
            coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
                Result.success(InstructionFetch(secondExecute)),
                Result.success(InstructionFetch(end)),
            )

            val result = interactor(params)

            assertEquals(PaymentFlowResult.Completed(end), result.getOrNull())
            coVerify(exactly = 1) { orchestrator.start(PAYMENT_METHOD_TYPE, InstructionFetch(execute)) }
            coVerify(exactly = 1) { orchestrator.start(PAYMENT_METHOD_TYPE, InstructionFetch(secondExecute)) }
        }

    @Test
    fun `should pace polling by the instruction poll delay`() = runTest(testDispatcher) {
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(wait))
        coEvery { repository.fetchInstructions(any()) } returns Result.success(InstructionFetch(end))

        val deferred = async { interactor(params) }
        testScheduler.runCurrent()
        testScheduler.advanceTimeBy(POLL_DELAY - 1)
        testScheduler.runCurrent()
        coVerify(exactly = 0) { repository.fetchInstructions(any()) }

        testScheduler.advanceTimeBy(1)
        testScheduler.runCurrent()
        coVerify(exactly = 1) { repository.fetchInstructions(CLIENT_SESSION_ID) }

        assertEquals(PaymentFlowResult.Completed(end), deferred.await().getOrNull())
    }

    @Test
    fun `should pass the Execute envelope currentAttempt to the orchestrator`() =
        runTest(testDispatcher) {
            val currentAttempt = CurrentAttempt(
                id = "attempt-1",
                paymentInstrumentTokenId = "token-1",
                paymentId = "pay-1",
            )
            val envelope = InstructionFetch(
                instruction = execute,
                currentAttempt = currentAttempt,
            )
            coEvery { repository.start(any(), any(), any()) } returns Result.success(envelope)
            coEvery {
                orchestrator.start(any(), any())
            } returns Result.success(CheckoutFlowOutcome.Completed)
            coEvery { repository.fetchInstructions(any()) } returns Result.success(InstructionFetch(end))

            interactor(params)

            coVerify(exactly = 1) {
                orchestrator.start(PAYMENT_METHOD_TYPE, envelope)
            }
        }

    @Test
    fun `should fail the flow when a poll fails while the orchestrator runs`() = runTest(testDispatcher) {
        val exception = RuntimeException("poll failed")
        coEvery { repository.start(any(), any(), any()) } returns Result.success(InstructionFetch(execute))
        val orchestratorGate = CompletableDeferred<Result<CheckoutFlowOutcome>>()
        coEvery { orchestrator.start(any(), any()) } coAnswers { orchestratorGate.await() }
        coEvery { repository.fetchInstructions(any()) } returns Result.failure(exception)

        val result = interactor(params)

        val failure = result.exceptionOrNull()
        assertTrue(failure is RuntimeException)
        assertEquals(exception.message, failure?.message)
    }

    private companion object {
        const val PAYMENT_METHOD_TYPE = "PAYMENT_CARD"
        const val RETURN_URI = "primer://requestor.com.example.app/async"
        const val CLIENT_SESSION_ID = "session_123"
        const val PAYLOAD = """{"action":"tokenize"}"""
        const val SECOND_PAYLOAD = """{"action":"redirect"}"""
        const val POLL_DELAY = 100L
    }
}
