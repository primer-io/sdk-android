@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.data

import android.util.Base64
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.InstantExecutorExtension
import io.primer.checkout.orchestrator.ManifestOverrides
import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.model.CelTarget
import io.primer.checkout.orchestrator.data.model.ResourceInfo
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest.CelInfo
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest.StateProcessorInfo
import io.primer.checkout.orchestrator.data.model.WasmResourceInfo
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorException
import io.primer.checkout.orchestrator.domain.model.CheckoutFlowOutcome
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import io.primer.executionengine.domain.registry.StepExecutorRegistry
import io.primer.jscore.domain.core.JsExecutor
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.CurrentAttempt
import io.primer.statetransport.domain.model.InstructionFetch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class DefaultCheckoutOrchestratorTest {

    @MockK
    lateinit var jsExecutor: JsExecutor

    @MockK
    lateinit var stepExecutorRegistry: StepExecutorRegistry

    @MockK
    lateinit var manifestRemoteDataSource: ManifestRemoteDataSource

    @MockK
    lateinit var configurationDataSource: CacheConfigurationDataSource

    @MockK
    lateinit var sdkContextProvider: SdkContextProvider

    @MockK
    lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var orchestrator: DefaultCheckoutOrchestrator

    private val manifest = StateProcessorManifest(
        stateProcessor = StateProcessorInfo(
            version = "1.0.0",
            umd = ResourceInfo(url = "https://example.com/sp.js", sha256 = "sp-sha"),
        ),
        cel = CelInfo(
            version = "1.0.0",
            noModules = CelTarget(
                js = ResourceInfo(url = "https://example.com/cel.js", sha256 = "cel-js-sha"),
                wasm = WasmResourceInfo(
                    url = "https://example.com/cel.wasm",
                    sha256 = "cel-wasm-sha",
                    gz = "https://example.com/cel.wasm.gz",
                ),
            ),
        ),
    )

    private val payload = payloadWithParameters(JSONObject().put("param", "data"))

    private val paymentMethodType = "PAYMENT_CARD"

    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } returns byteArrayOf(1, 2, 3)

        mockkObject(ManifestOverrides)
        every { ManifestOverrides.url } returns null

        val configurationData = mockk<ConfigurationData>(relaxed = true)
        every { configurationData.environment } returns Environment.SANDBOX
        every { configurationData.pciUrl } returns PCI_URL
        every { configurationData.coreUrl } returns CORE_URL
        every { configurationDataSource.get() } returns configurationData

        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        coEvery { manifestRemoteDataSource.fetchManifest(any()) } returns manifest
        every { sdkContextProvider.provide(any(), any()) } returns """{"sdk":"context"}"""
        every { stepExecutorRegistry.onFinish() } just Runs

        orchestrator = DefaultCheckoutOrchestrator(
            jsExecutor = jsExecutor,
            stepExecutorRegistry = stepExecutorRegistry,
            manifestRemoteDataSource = manifestRemoteDataSource,
            configurationDataSource = configurationDataSource,
            sdkContextProvider = sdkContextProvider,
            analyticsInteractor = analyticsInteractor,
            trustedPublicKeysB64 = listOf("dGVzdA=="),
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Base64::class)
        unmockkObject(ManifestOverrides)
    }

    @Test
    fun `start() should return Completed when state processor returns terminal success`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = startOrchestrator(payload)

        assertTrue(result.isSuccess)
        assertEquals(CheckoutFlowOutcome.Completed, result.getOrNull())
    }

    @Test
    fun `start() should return Cancelled when terminal outcome is CANCELLED`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.CANCELLED))

        val result = startOrchestrator(payload)

        assertTrue(result.isSuccess)
        assertEquals(CheckoutFlowOutcome.Cancelled, result.getOrNull())
    }

    @Test
    fun `start() should throw TerminalErrorException when terminal outcome is ERROR`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.ERROR))

        val result = startOrchestrator(payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.TerminalErrorException)
    }

    @Test
    fun `start() should throw TerminalErrorException when terminal outcome is UNSUPPORTED`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.UNSUPPORTED))

        val result = startOrchestrator(payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.TerminalErrorException)
    }

    @Test
    fun `start() should throw StateProcessorException when result has error`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(errorResultJson("ERR_001", "Something failed", "diag-abc"))

        val result = startOrchestrator(payload)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as CheckoutOrchestratorException.StateProcessorException
        assertEquals("ERR_001", exception.code)
        assertEquals("diag-abc", exception.stateProcessorDiagnosticsId)
        assertEquals("Something failed", exception.stateProcessorMessage)
    }

    @Test
    fun `start() should return Pending when result has no action, terminal, or error`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(emptyResultJson())

        val result = startOrchestrator(payload)

        assertTrue(result.isSuccess)
        assertEquals(CheckoutFlowOutcome.Pending, result.getOrNull())
    }

    @Test
    fun `start() should preserve schema parameters and merge SDK-owned keys into the initial state`() = runTest {
        val stateSlot = slot<String>()
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), capture(stateSlot), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))
        val currentAttempt = CurrentAttempt(
            id = "attempt-1",
            paymentInstrumentTokenId = "token-1",
            paymentId = "pay-1",
        )

        startOrchestrator(payload, currentAttempt)

        val state = JSONObject(stateSlot.captured)
        assertEquals("data", state.getString("param"))
        assertEquals(PCI_URL, state.getJSONObject("sdk").getString("pciUrl"))
        assertEquals(CORE_URL, state.getJSONObject("sdk").getString("coreUrl"))
        val attempt = state.getJSONObject("currentAttempt")
        assertEquals("attempt-1", attempt.getString("id"))
        assertEquals("token-1", attempt.getString("paymentInstrumentTokenId"))
        assertEquals("pay-1", attempt.getString("paymentId"))
    }

    @Test
    fun `start() should overwrite colliding schema-provided sdk and currentAttempt values`() = runTest {
        val stateSlot = slot<String>()
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), capture(stateSlot), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))
        val collidingPayload = payloadWithParameters(
            JSONObject().apply {
                put("sdk", JSONObject().put("pciUrl", "https://schema-pci.example.com"))
                put("currentAttempt", JSONObject().put("id", "schema-attempt"))
            },
        )
        val currentAttempt = CurrentAttempt(id = "attempt-1")

        startOrchestrator(collidingPayload, currentAttempt)

        val state = JSONObject(stateSlot.captured)
        assertEquals(PCI_URL, state.getJSONObject("sdk").getString("pciUrl"))
        assertEquals(CORE_URL, state.getJSONObject("sdk").getString("coreUrl"))
        assertEquals("attempt-1", state.getJSONObject("currentAttempt").getString("id"))
    }

    @Test
    fun `start() should remove schema-provided currentAttempt when envelope values are null`() =
        runTest {
            val stateSlot = slot<String>()
            coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
            coEvery {
                jsExecutor.initializeStateProcessor(any(), capture(stateSlot), any())
            } returns Result.success(terminalResultJson(Outcome.SUCCESS))
            val collidingPayload = payloadWithParameters(
                JSONObject().apply {
                    put("currentAttempt", JSONObject().put("id", "schema-attempt"))
                },
            )

            startOrchestrator(collidingPayload)

            val state = JSONObject(stateSlot.captured)
            assertFalse(state.has("currentAttempt"))
        }

    @Test
    fun `start() should pass paymentId from currentAttempt to the sdk context provider`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", "{}"))
        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.success(StepResult(outcome = Outcome.SUCCESS, actionId = "action-1"))
        coEvery {
            jsExecutor.applyResult(any(), any(), any(), any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))
        val currentAttempt = CurrentAttempt(id = "attempt-1", paymentId = "pay-1")

        startOrchestrator(payload, currentAttempt = currentAttempt)

        verify(exactly = 2) { sdkContextProvider.provide(paymentMethodType, "pay-1") }
    }

    @Test
    fun `start() should execute action and process next result when result has action`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", """{"token":"abc"}"""))

        val stepData = mapOf("result" to "ok")
        coEvery {
            stepExecutorRegistry.executeAction("action-1", "TOKENIZE", """{"token":"abc"}""")
        } returns Result.success(
            StepResult(outcome = Outcome.SUCCESS, actionId = "action-1", data = stepData),
        )

        coEvery {
            jsExecutor.applyResult(any(), any(), any(), eq("action-1"), eq("success"), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = startOrchestrator(payload)

        assertTrue(result.isSuccess)
        assertEquals(CheckoutFlowOutcome.Completed, result.getOrNull())
        coVerify {
            stepExecutorRegistry.executeAction("action-1", "TOKENIZE", """{"token":"abc"}""")
            jsExecutor.applyResult(any(), any(), any(), "action-1", "success", any())
        }
    }

    @Test
    fun `start() should marshal step result data as real JSON when applying the result`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", "{}"))

        val stepData = mapOf("result" to "ok", "nested" to mapOf("count" to 2))
        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.success(
            StepResult(outcome = Outcome.SUCCESS, actionId = "action-1", data = stepData),
        )

        val responseSlot = slot<String>()
        coEvery {
            jsExecutor.applyResult(any(), any(), any(), any(), any(), capture(responseSlot))
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = startOrchestrator(payload)

        assertTrue(result.isSuccess)
        val response = JSONObject(responseSlot.captured)
        assertEquals("ok", response.getString("result"))
        assertEquals(2, response.getJSONObject("nested").getInt("count"))
    }

    @Test
    fun `start() should marshal empty step data as an empty JSON object`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", "{}"))
        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.success(StepResult(outcome = Outcome.SUCCESS, actionId = "action-1"))

        val responseSlot = slot<String>()
        coEvery {
            jsExecutor.applyResult(any(), any(), any(), any(), any(), capture(responseSlot))
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = startOrchestrator(payload)

        assertTrue(result.isSuccess)
        assertEquals(0, JSONObject(responseSlot.captured).length())
    }

    @Test
    fun `start() should feed error with message back to state processor when step execution fails`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "HTTP", """{"url":"https://pay.example.com"}"""))

        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.failure(RuntimeException("pay endpoint failed"))

        val responseSlot = slot<String>()
        coEvery {
            jsExecutor.applyResult(any(), any(), any(), eq("action-1"), eq("error"), capture(responseSlot))
        } returns Result.success(terminalResultJson(Outcome.ERROR))

        val result = startOrchestrator(payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.TerminalErrorException)
        assertEquals("pay endpoint failed", JSONObject(responseSlot.captured).getString("message"))
    }

    @Test
    fun `start() should wait delayMs before dispatching the action to the registry`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", "{}", delayMs = DELAY_MS))
        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.success(StepResult(outcome = Outcome.SUCCESS, actionId = "action-1"))
        coEvery {
            jsExecutor.applyResult(any(), any(), any(), any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val job = launch { startOrchestrator(payload) }
        runCurrent()
        advanceTimeBy(DELAY_MS - 1)
        runCurrent()
        coVerify(exactly = 0) { stepExecutorRegistry.executeAction(any(), any(), any()) }

        advanceTimeBy(1)
        runCurrent()
        coVerify(exactly = 1) { stepExecutorRegistry.executeAction(any(), any(), any()) }
        job.join()
    }

    @Test
    fun `start() should dispatch the action immediately when delayMs is absent`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", "{}"))
        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.success(StepResult(outcome = Outcome.SUCCESS, actionId = "action-1"))
        coEvery {
            jsExecutor.applyResult(any(), any(), any(), any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val job = launch { startOrchestrator(payload) }
        runCurrent()

        coVerify(exactly = 1) { stepExecutorRegistry.executeAction(any(), any(), any()) }
        job.join()
    }

    @Test
    fun `start() should not dispatch or apply results when aborted during the delayMs wait`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", "{}", delayMs = DELAY_MS))

        val job = launch { startOrchestrator(payload) }
        runCurrent()
        job.cancel()
        runCurrent()

        coVerify(exactly = 0) { stepExecutorRegistry.executeAction(any(), any(), any()) }
        coVerify(exactly = 0) { jsExecutor.applyResult(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) { stepExecutorRegistry.onFinish() }
    }

    @Test
    fun `start() should invoke registry onFinish on terminal outcome`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        startOrchestrator(payload)

        verify(exactly = 1) { stepExecutorRegistry.onFinish() }
    }

    @Test
    fun `start() should invoke registry onFinish on failure`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.failure(RuntimeException("init failed"))

        val result = startOrchestrator(payload)

        assertTrue(result.isFailure)
        verify(exactly = 1) { stepExecutorRegistry.onFinish() }
    }

    @Test
    fun `start() should use ManifestOverrides url when set`() = runTest {
        every { ManifestOverrides.url } returns "https://custom-manifest.com/manifest.json"

        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        startOrchestrator(payload)

        coVerify { manifestRemoteDataSource.fetchManifest("https://custom-manifest.com/manifest.json") }
    }

    @Test
    fun `start() should build manifest url from environment when ManifestOverrides url is null`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        startOrchestrator(payload)

        coVerify {
            manifestRemoteDataSource.fetchManifest("https://sdk.primer.io/state-processor/v0/manifests/sandbox.json")
        }
    }

    @Test
    fun `start() should propagate failure when jsExecutor initialize fails`() = runTest {
        val error = RuntimeException("init failed")
        coEvery { jsExecutor.initialize(any()) } returns Result.failure(error)

        val result = startOrchestrator(payload)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `start() should log analytics event on start`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        startOrchestrator(payload)

        coVerify { analyticsInteractor(any()) }
    }

    private fun payloadWithParameters(parameters: JSONObject): String = JSONObject().apply {
        put("schema", JSONObject().put("key", "value"))
        put("parameters", parameters)
    }.toString()

    private fun terminalResultJson(outcome: Outcome): String = JSONObject().apply {
        put("newState", JSONObject().put("s", "1"))
        put("terminal", JSONObject().put("outcome", outcome.value))
    }.toString()

    private fun errorResultJson(code: String, message: String, diagnosticsId: String): String =
        JSONObject().apply {
            put("newState", JSONObject().put("s", "1"))
            put(
                "error",
                JSONObject().apply {
                    put("code", code)
                    put("message", message)
                    put("diagnosticsId", diagnosticsId)
                },
            )
        }.toString()

    private fun actionResultJson(id: String, type: String, params: String, delayMs: Long? = null): String =
        JSONObject().apply {
            put("newState", JSONObject().put("s", "1"))
            put(
                "action",
                JSONObject().apply {
                    put("id", id)
                    put("type", type)
                    put("params", params)
                    delayMs?.let { put("delayMs", it) }
                },
            )
        }.toString()

    private fun emptyResultJson(): String = JSONObject().apply {
        put("newState", JSONObject().put("s", "1"))
    }.toString()

    @Test
    fun `start should fail when the envelope instruction is not Execute`() = runTest {
        val result = orchestrator.start(
            paymentMethodType = paymentMethodType,
            envelope = InstructionFetch(ClientInstructions.Wait(pollDelayMilliseconds = 0L)),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    private suspend fun startOrchestrator(
        payload: String,
        currentAttempt: CurrentAttempt? = null,
    ) = orchestrator.start(
        paymentMethodType = paymentMethodType,
        envelope = InstructionFetch(
            instruction = ClientInstructions.Execute(pollDelayMilliseconds = 0L, payload = payload),
            currentAttempt = currentAttempt,
        ),
    )

    private companion object {
        const val PCI_URL = "https://pci.example.com"
        const val CORE_URL = "https://core.example.com"
        const val DELAY_MS = 500L
    }
}
