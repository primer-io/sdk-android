@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.statetransport.domain.interactor

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.domain.model.ClientSession
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.repository.StateTransportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
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

    private lateinit var interactor: PaymentFlowInteractor

    private val testDispatcher = StandardTestDispatcher()

    private val params = PaymentFlowInteractor.PaymentFlowParams(
        paymentMethodType = "PAYMENT_CARD",
        returnUri = "https://return.example.com",
    )

    @BeforeEach
    fun setUp() {
        val clientSessionDataResponse = mockk<ClientSessionDataResponse>(relaxed = true) {
            every { clientSessionId } returns "session_123"
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
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `should emit End and complete when pay returns End`() = runTest(testDispatcher) {
        val end = ClientInstructions.End(checkoutOutcome = null, payment = null)
        coEvery { repository.start(any(), any(), any()) } returns Result.success(end)

        val emissions = interactor.execute(params).toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions[0] is ClientInstructions.End)
    }

    @Test
    fun `should emit Execute then poll and emit End`() = runTest(testDispatcher) {
        val execute = ClientInstructions.Execute(pollDelayMilliseconds = 100L, payload = "{}")
        val end = ClientInstructions.End(checkoutOutcome = null, payment = null)

        coEvery { repository.start(any(), any(), any()) } returns Result.success(execute)
        coEvery { repository.fetchInstructions(any()) } returns Result.success(end)

        val emissions = interactor.execute(params).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is ClientInstructions.Execute)
        assertTrue(emissions[1] is ClientInstructions.End)
    }

    @Test
    fun `should poll with Wait then emit End`() = runTest(testDispatcher) {
        val wait = ClientInstructions.Wait(pollDelayMilliseconds = 100L)
        val end = ClientInstructions.End(checkoutOutcome = null, payment = null)

        coEvery { repository.start(any(), any(), any()) } returns Result.success(wait)
        coEvery { repository.fetchInstructions(any()) } returns Result.success(end)

        val emissions = interactor.execute(params).toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions[0] is ClientInstructions.End)
    }

    @Test
    fun `should emit multiple Execute instructions before End`() = runTest(testDispatcher) {
        val execute1 = ClientInstructions.Execute(pollDelayMilliseconds = 100L, payload = """{"step":1}""")
        val execute2 = ClientInstructions.Execute(pollDelayMilliseconds = 100L, payload = """{"step":2}""")
        val end = ClientInstructions.End(checkoutOutcome = null, payment = null)

        coEvery { repository.start(any(), any(), any()) } returns Result.success(
            ClientInstructions.Wait(pollDelayMilliseconds = 100L),
        )
        coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
            Result.success(execute1),
            Result.success(execute2),
            Result.success(end),
        )

        val emissions = interactor.execute(params).toList()

        assertEquals(3, emissions.size)
        assertTrue(emissions[0] is ClientInstructions.Execute)
        assertTrue(emissions[1] is ClientInstructions.Execute)
        assertTrue(emissions[2] is ClientInstructions.End)
    }

    @Test
    fun `should skip Wait during polling and not emit it`() = runTest(testDispatcher) {
        val waitPoll = ClientInstructions.Wait(pollDelayMilliseconds = 100L)
        val end = ClientInstructions.End(checkoutOutcome = null, payment = null)

        coEvery { repository.start(any(), any(), any()) } returns Result.success(
            ClientInstructions.Wait(pollDelayMilliseconds = 100L),
        )
        coEvery { repository.fetchInstructions(any()) } returnsMany listOf(
            Result.success(waitPoll),
            Result.success(end),
        )

        val emissions = interactor.execute(params).toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions[0] is ClientInstructions.End)
    }

    @Test
    fun `should throw when pay fails`() = runTest(testDispatcher) {
        val error = RuntimeException("pay failed")
        coEvery { repository.start(any(), any(), any()) } returns Result.failure(error)

        val result = runCatching { interactor.execute(params).toList() }

        assertTrue(result.isFailure)
        assertEquals("pay failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should throw when fetchInstructions fails during polling`() = runTest(testDispatcher) {
        coEvery { repository.start(any(), any(), any()) } returns Result.success(
            ClientInstructions.Wait(pollDelayMilliseconds = 100L),
        )
        coEvery { repository.fetchInstructions(any()) } returns Result.failure(
            RuntimeException("poll failed"),
        )

        val result = runCatching { interactor.execute(params).toList() }

        assertTrue(result.isFailure)
        assertEquals("poll failed", result.exceptionOrNull()?.message)
    }
}
