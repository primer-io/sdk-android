@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.primer.checkout.orchestrator.ManifestOverrides
import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.model.CelTarget
import io.primer.checkout.orchestrator.data.model.ResourceInfo
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest.CelInfo
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest.StateProcessorInfo
import io.primer.checkout.orchestrator.data.model.WasmResourceInfo
import io.primer.checkout.orchestrator.data.model.jsResources
import io.primer.jscore.domain.core.JsExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class StateProcessorLoaderTest {

    @MockK
    lateinit var manifestRemoteDataSource: ManifestRemoteDataSource

    @MockK
    lateinit var jsExecutor: JsExecutor

    private var alive = false
    private lateinit var loader: StateProcessorLoader

    @BeforeEach
    fun setUp() {
        mockkObject(ManifestOverrides)
        every { ManifestOverrides.url } returns null

        coEvery { manifestRemoteDataSource.fetchManifest(any()) } returns manifest
        every { jsExecutor.isAlive } answers { alive }
        coEvery { jsExecutor.initialize(any()) } answers {
            alive = true
            Result.success(Unit)
        }
        loader = StateProcessorLoader(manifestRemoteDataSource, jsExecutor)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(ManifestOverrides)
    }

    @Test
    fun `loads the environment's bundle once, for every caller`() = runTest {
        assertSame(jsExecutor, loader.load(SANDBOX))
        assertSame(jsExecutor, loader.load(SANDBOX))

        coVerify(exactly = 1) { manifestRemoteDataSource.fetchManifest(SANDBOX_MANIFEST) }
        coVerify(exactly = 1) { jsExecutor.initialize(manifest.jsResources) }
    }

    @Test
    fun `loads again after a failed load`() = runTest {
        coEvery { jsExecutor.initialize(any()) } returns Result.failure(IllegalStateException("boom")) andThen
            Result.success(Unit)

        assertThrows<IllegalStateException> { loader.load(SANDBOX) }
        assertSame(jsExecutor, loader.load(SANDBOX))
        coVerify(exactly = 2) { jsExecutor.initialize(any()) }
    }

    @Test
    fun `loads again after a cancelled load`() = runTest {
        coEvery { manifestRemoteDataSource.fetchManifest(any()) } coAnswers { awaitCancellation() } andThen manifest

        val cancelled = launch { loader.load(SANDBOX) }
        runCurrent()
        cancelled.cancel()

        assertSame(jsExecutor, loader.load(SANDBOX))
        coVerify(exactly = 2) { manifestRemoteDataSource.fetchManifest(SANDBOX_MANIFEST) }
    }

    @Test
    fun `loads again once the engine died`() = runTest {
        loader.load(SANDBOX)
        alive = false

        loader.load(SANDBOX)

        coVerify(exactly = 2) { jsExecutor.initialize(any()) }
    }

    @Test
    fun `runs a call again on a new load when the engine died during it`() = runTest {
        coEvery { jsExecutor.checkClientRequirements(any(), any()) } answers {
            alive = false
            Result.failure(IllegalStateException("sandbox dead"))
        } andThen Result.success(VERDICTS)

        assertEquals(VERDICTS, loader.execute(SANDBOX) { checkClientRequirements("[]", "{}") })
        coVerify(exactly = 2) { jsExecutor.initialize(any()) }
    }

    @Test
    fun `does not run a call again when it failed on a live engine`() = runTest {
        coEvery { jsExecutor.checkClientRequirements(any(), any()) } returns
            Result.failure(IllegalStateException("TypeError"))

        assertThrows<IllegalStateException> { loader.execute<String>(SANDBOX) { checkClientRequirements("[]", "{}") } }
        coVerify(exactly = 1) { jsExecutor.checkClientRequirements(any(), any()) }
    }

    private companion object {
        const val SANDBOX = "sandbox"
        const val SANDBOX_MANIFEST = "https://sdk.primer.io/state-processor/v0/manifests/sandbox.json"
        const val VERDICTS = """{"TWINT":{"satisfied":true,"unmet":[]}}"""

        val manifest = StateProcessorManifest(
            stateProcessor = StateProcessorInfo(
                version = "1.0.0",
                umd = ResourceInfo(
                    url = "https://example.com/sp.js",
                    sha256 = "sp-sha",
                    gz = "https://example.com/sp.js.gz",
                ),
            ),
            cel = CelInfo(
                version = "1.0.0",
                noModules = CelTarget(
                    js = ResourceInfo(
                        url = "https://example.com/cel.js",
                        sha256 = "cel-js-sha",
                        gz = "https://example.com/cel.js.gz",
                    ),
                    wasm = WasmResourceInfo(
                        url = "https://example.com/cel.wasm",
                        sha256 = "cel-wasm-sha",
                        gz = "https://example.com/cel.wasm.gz",
                    ),
                ),
            ),
        )
    }
}
