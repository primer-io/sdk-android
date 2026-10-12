@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.data.model.PaymentMethodCapability
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.configuration.data.model.PaymentMethodImplementationType
import io.primer.android.core.logging.internal.LogReporter
import io.primer.checkout.orchestrator.data.ClientCapabilitiesProvider
import io.primer.checkout.orchestrator.data.datasource.ClientRequirementsDataSource
import io.primer.checkout.orchestrator.data.model.ClientCapabilities
import io.primer.checkout.orchestrator.data.model.ProbeRequest
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import io.primer.executionengine.domain.probe.ProbeOutcome
import io.primer.executionengine.domain.probe.ProbeResult
import io.primer.executionengine.domain.registry.ProbeRegistry
import io.primer.executionengine.domain.registry.StepExecutorRegistry
import io.primer.executionengine.domain.registry.UiComponentRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.milliseconds

@ExtendWith(MockKExtension::class)
internal class ClientRequirementsDataRepositoryTest {

    @MockK
    lateinit var clientRequirementsDataSource: ClientRequirementsDataSource

    @MockK
    lateinit var configurationDataSource: CacheConfigurationDataSource

    @MockK
    lateinit var sdkContextProvider: SdkContextProvider

    @MockK
    lateinit var stepExecutorRegistry: StepExecutorRegistry

    @MockK
    lateinit var uiComponentRegistry: UiComponentRegistry

    @MockK
    lateinit var probeRegistry: ProbeRegistry

    private val logReporter = mockk<LogReporter>(relaxed = true)

    @BeforeEach
    fun setUp() {
        givenPaymentMethods(twint, card, legacyIdeal)
        coEvery { clientRequirementsDataSource.probesToRun(any(), any(), any(), any()) } returns emptyList()
        coEvery { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) } returns VERDICTS
        every { sdkContextProvider.provide(null, null) } returns CONTEXT
        every { stepExecutorRegistry.declaredVersions } returns STEP_TYPES
        every { uiComponentRegistry.declaredVersions } returns UI_COMPONENTS
        every { probeRegistry.declaredVersions } returns PROBES
    }

    @Test
    fun `checks the backend-driven methods, with this SDK's capabilities and context`() = runTest {
        assertEquals(VERDICTS, repository().getVerdicts().getOrThrow())

        val capabilities =
            ClientCapabilities(stepTypes = STEP_TYPES, uiComponents = UI_COMPONENTS, probes = PROBES)
        coVerify {
            clientRequirementsDataSource.check("sandbox", listOf(twint, legacyIdeal), capabilities, CONTEXT, emptyMap())
        }
    }

    @Test
    fun `runs the probes the processor asks for and checks with their results by key`() = runTest {
        val request = ProbeRequest(KLARNA_KEY, "klarna.sdk", mapOf("version" to "^2.16.0"))
        val found = ProbeResult(ProbeOutcome.SUCCESS, mapOf("version" to "2.16.0"))
        coEvery { clientRequirementsDataSource.probesToRun(any(), any(), any(), any()) } returns listOf(request)
        coEvery { probeRegistry.run("klarna.sdk", mapOf("version" to "^2.16.0")) } returns found

        repository().getVerdicts()

        coVerify { clientRequirementsDataSource.check(any(), any(), any(), any(), mapOf(KLARNA_KEY to found)) }
    }

    @Test
    fun `declares the UI components the host registered by the time the check runs`() = runTest {
        every { uiComponentRegistry.declaredVersions } returns emptyMap()
        val sut = repository()
        every { uiComponentRegistry.declaredVersions } returns UI_COMPONENTS

        sut.getVerdicts()

        coVerify {
            clientRequirementsDataSource.check(any(), any(), match { it.uiComponents == UI_COMPONENTS }, any(), any())
        }
    }

    @Test
    fun `declares the probes the modules registered by the time the check runs`() = runTest {
        every { probeRegistry.declaredVersions } returns emptyMap()
        val sut = repository()
        every { probeRegistry.declaredVersions } returns PROBES

        sut.getVerdicts()

        coVerify {
            clientRequirementsDataSource.check(any(), any(), match { it.probes == PROBES }, any(), any())
        }
    }

    @Test
    fun `checks once per configuration`() = runTest {
        val sut = repository()

        sut.getVerdicts()
        sut.getVerdicts()

        coVerify(exactly = 1) { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `holds a failed check for the configuration`() = runTest {
        coEvery { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) } throws
            IllegalStateException("boom")
        val sut = repository()

        assertTrue(sut.getVerdicts().isFailure)
        assertTrue(sut.getVerdicts().isFailure)

        coVerify(exactly = 1) { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `checks again for a new configuration`() = runTest {
        val sut = repository()

        sut.getVerdicts()
        givenPaymentMethods(twint)
        sut.getVerdicts()

        coVerify(exactly = 2) { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `fails once the check overruns its time`() = runTest {
        coEvery { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) } coAnswers {
            delay(10_000)
            VERDICTS
        }

        val result = repository(timeout = 50).getVerdicts()

        assertInstanceOf(ClientRequirementsTimeoutException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `checks again after a check cancelled with its caller`() = runTest {
        coEvery { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) } coAnswers
            { awaitCancellation() } andThen VERDICTS
        val sut = repository()

        val cancelled = launch { sut.getVerdicts() }
        runCurrent()
        cancelled.cancel()

        assertEquals(VERDICTS, sut.getVerdicts().getOrThrow())
    }

    @Test
    fun `checks nothing without a backend-driven method`() = runTest {
        givenPaymentMethods(card)

        assertTrue(repository().getVerdicts().getOrThrow().isEmpty())

        coVerify(exactly = 0) { clientRequirementsDataSource.check(any(), any(), any(), any(), any()) }
    }

    private fun repository(timeout: Long = 5_000) = ClientRequirementsDataRepository(
        clientRequirementsDataSource = clientRequirementsDataSource,
        configurationDataSource = configurationDataSource,
        sdkContextProvider = sdkContextProvider,
        capabilitiesProvider = ClientCapabilitiesProvider(
            stepExecutorRegistry = stepExecutorRegistry,
            uiComponentRegistry = uiComponentRegistry,
            probeRegistry = probeRegistry,
        ),
        probeRegistry = probeRegistry,
        logReporter = logReporter,
        timeout = timeout.milliseconds,
    )

    private fun givenPaymentMethods(vararg methods: PaymentMethodConfigDataResponse) {
        val configuration = mockk<ConfigurationData>(relaxed = true)
        every { configuration.environment } returns Environment.SANDBOX
        every { configuration.paymentMethods } returns methods.toList()
        every { configurationDataSource.get() } returns configuration
    }

    private companion object {
        const val KLARNA_KEY = """klarna.sdk#{"version":"^2.16.0"}"""
        const val CONTEXT = """{"sdk":{"type":"ANDROID_NATIVE"}}"""
        val STEP_TYPES = mapOf("url.open" to "1.0.0")
        val UI_COMPONENTS = mapOf("SelectionGroup" to "1.0.0", "klarna.widget" to "1.0.0")
        val PROBES = mapOf("klarna.sdk" to "1.0.0")
        val VERDICTS = mapOf("TWINT" to ClientRequirementsCheck(satisfied = true, unmet = "[]"))

        val twint = method("pm_twint", "TWINT", PaymentMethodImplementationType.BACKEND_DRIVEN)
        val card = method("pm_card", "PAYMENT_CARD", PaymentMethodImplementationType.NATIVE_SDK)
        val legacyIdeal = method(
            "pm_ideal",
            "ADYEN_IDEAL",
            PaymentMethodImplementationType.WEB_REDIRECT,
            capabilities = setOf(PaymentMethodCapability.BACKEND_DRIVEN),
        )

        fun method(
            id: String,
            type: String,
            implementationType: PaymentMethodImplementationType,
            capabilities: Set<PaymentMethodCapability> = emptySet(),
        ) = PaymentMethodConfigDataResponse(
            id = id,
            name = null,
            implementationType = implementationType,
            type = type,
            options = null,
            displayMetadata = null,
            capabilities = capabilities,
        )
    }
}
