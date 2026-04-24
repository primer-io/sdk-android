@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.data

import android.util.Base64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
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
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import io.primer.executionengine.domain.registry.StepExecutorRegistry
import io.primer.jscore.domain.core.JsExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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

    private val payload = JSONObject().apply {
        put("schema", JSONObject().put("key", "value"))
        put("parameters", JSONObject().put("param", "data"))
    }.toString()

    private val paymentMethodType = "PAYMENT_CARD"

    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } returns byteArrayOf(1, 2, 3)

        mockkObject(ManifestOverrides)
        every { ManifestOverrides.url } returns null

        val configurationData = io.mockk.mockk<ConfigurationData>(relaxed = true)
        every { configurationData.environment } returns Environment.SANDBOX
        every { configurationDataSource.get() } returns configurationData

        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        coEvery { manifestRemoteDataSource.fetchManifest(any()) } returns manifest
        every { sdkContextProvider.provide(any()) } returns """{"sdk":"context"}"""

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
    fun `start() should return SUCCESS when state processor returns terminal success`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isSuccess)
        assertEquals(Outcome.SUCCESS, result.getOrNull())
    }

    @Test
    fun `start() should return CANCELLED when terminal outcome is CANCELLED`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.CANCELLED))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isSuccess)
        assertEquals(Outcome.CANCELLED, result.getOrNull())
    }

    @Test
    fun `start() should throw TerminalErrorException when terminal outcome is ERROR`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.ERROR))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.TerminalErrorException)
    }

    @Test
    fun `start() should throw TerminalErrorException when terminal outcome is UNSUPPORTED`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.UNSUPPORTED))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.TerminalErrorException)
    }

    @Test
    fun `start() should throw StateProcessorException when result has error`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(errorResultJson("ERR_001", "Something failed", "diag-abc"))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as CheckoutOrchestratorException.StateProcessorException
        assertEquals("ERR_001", exception.code)
        assertEquals("diag-abc", exception.stateProcessorDiagnosticsId)
        assertEquals("Something failed", exception.stateProcessorMessage)
    }

    @Test
    fun `start() should throw MissingActionException when result has no action, terminal, or error`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(emptyResultJson())

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.MissingActionException)
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
            jsExecutor.applyResult(any(), any(), any(), eq("action-1"), eq("success"), eq(stepData.toString()))
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isSuccess)
        assertEquals(Outcome.SUCCESS, result.getOrNull())
        coVerify {
            stepExecutorRegistry.executeAction("action-1", "TOKENIZE", """{"token":"abc"}""")
            jsExecutor.applyResult(any(), any(), any(), "action-1", "success", stepData.toString())
        }
    }

    @Test
    fun `start() should use empty map toString when step result has default data`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "TOKENIZE", """{"token":"abc"}"""))

        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.success(
            StepResult(outcome = Outcome.SUCCESS, actionId = "action-1"),
        )

        coEvery {
            jsExecutor.applyResult(any(), any(), any(), any(), any(), eq(emptyMap<String, Any?>().toString()))
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isSuccess)
        coVerify {
            jsExecutor.applyResult(
                any(),
                any(),
                any(),
                "action-1",
                "success",
                emptyMap<String, Any?>().toString(),
            )
        }
    }

    @Test
    fun `start() should feed error back to state processor when step execution fails`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(actionResultJson("action-1", "HTTP", """{"url":"https://pay.example.com"}"""))

        coEvery {
            stepExecutorRegistry.executeAction(any(), any(), any())
        } returns Result.failure(RuntimeException("pay endpoint failed"))

        coEvery {
            jsExecutor.applyResult(any(), any(), any(), eq("action-1"), eq("error"), any())
        } returns Result.success(terminalResultJson(Outcome.ERROR))

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CheckoutOrchestratorException.TerminalErrorException)
        coVerify {
            jsExecutor.applyResult(any(), any(), any(), "action-1", "error", any())
        }
    }

    @Test
    fun `start() should use ManifestOverrides url when set`() = runTest {
        every { ManifestOverrides.url } returns "https://custom-manifest.com/manifest.json"

        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        orchestrator.start(paymentMethodType, payload)

        coVerify { manifestRemoteDataSource.fetchManifest("https://custom-manifest.com/manifest.json") }
    }

    @Test
    fun `start() should build manifest url from environment when ManifestOverrides url is null`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        orchestrator.start(paymentMethodType, payload)

        coVerify {
            manifestRemoteDataSource.fetchManifest("https://sdk.primer.io/state-processor/v0/manifests/sandbox.json")
        }
    }

    @Test
    fun `start() should propagate failure when jsExecutor initialize fails`() = runTest {
        val error = RuntimeException("init failed")
        coEvery { jsExecutor.initialize(any()) } returns Result.failure(error)

        val result = orchestrator.start(paymentMethodType, payload)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `start() should log analytics event on start`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.success(Unit)
        coEvery {
            jsExecutor.initializeStateProcessor(any(), any(), any())
        } returns Result.success(terminalResultJson(Outcome.SUCCESS))

        orchestrator.start(paymentMethodType, payload)

        coVerify { analyticsInteractor(any()) }
    }

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

    private fun actionResultJson(id: String, type: String, params: String): String =
        JSONObject().apply {
            put("newState", JSONObject().put("s", "1"))
            put(
                "action",
                JSONObject().apply {
                    put("id", id)
                    put("type", type)
                    put("params", params)
                },
            )
        }.toString()

    private fun emptyResultJson(): String = JSONObject().apply {
        put("newState", JSONObject().put("s", "1"))
    }.toString()
}
