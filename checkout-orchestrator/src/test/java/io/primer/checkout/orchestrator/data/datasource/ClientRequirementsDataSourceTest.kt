package io.primer.checkout.orchestrator.data.datasource

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.configuration.data.model.PaymentMethodImplementationType
import io.primer.checkout.orchestrator.data.StateProcessorLoader
import io.primer.checkout.orchestrator.data.model.ClientCapabilities
import io.primer.checkout.orchestrator.data.model.ProbeRequest
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import io.primer.executionengine.domain.probe.ProbeOutcome
import io.primer.executionengine.domain.probe.ProbeResult
import io.primer.jscore.domain.core.JsExecutor
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ClientRequirementsDataSourceTest {

    @MockK
    lateinit var stateProcessorLoader: StateProcessorLoader

    @MockK
    lateinit var jsExecutor: JsExecutor

    private val items = slot<String>()
    private val client = slot<String>()
    private val results = slot<String>()
    private lateinit var dataSource: ClientRequirementsDataSource

    @BeforeEach
    fun setUp() {
        coEvery { stateProcessorLoader.execute<String>(SANDBOX, any()) } coAnswers {
            secondArg<suspend JsExecutor.() -> Result<String>>().invoke(jsExecutor).getOrThrow()
        }
        coEvery { jsExecutor.probesToRun(any(), any()) } returns Result.success("[]")
        coEvery { jsExecutor.checkClientRequirements(capture(items), capture(client), capture(results)) } returns
            Result.success(VERDICTS)
        dataSource = ClientRequirementsDataSource(stateProcessorLoader)
    }

    @Test
    fun `sends each method's requirements, and what this SDK runs in the checkout's context`() = runTest {
        check()

        val sent = JSONArray(items.captured)
        assertEquals(2, sent.length())
        assertEquals("pm_twint", sent.getJSONObject(0).getString("id"))
        assertEquals("TWINT", sent.getJSONObject(0).getString("type"))
        assertFalse(sent.getJSONObject(0).has("clientRequirements"))
        assertEquals(
            "^1.0.0",
            sent.getJSONObject(1).getJSONObject("clientRequirements").getJSONObject("stepTypes").getString("url.open"),
        )

        val capabilities = JSONObject(client.captured).getJSONObject("capabilities")
        assertEquals("1.0.0", capabilities.getJSONObject("stepTypes").getString("url.open"))
        assertEquals("1.0.0", capabilities.getJSONObject("uiComponents").getString("qr.code"))
        assertEquals("1.0.0", capabilities.getJSONObject("probes").getString("klarna.sdk"))
        assertEquals(
            "ANDROID_NATIVE",
            JSONObject(client.captured).getJSONObject("context").getJSONObject("sdk").getString("type"),
        )
    }

    @Test
    fun `reads the probes the processor asks for`() = runTest {
        coEvery { jsExecutor.probesToRun(any(), any()) } returns Result.success(KLARNA_PROBE)

        val requests = dataSource.probesToRun(SANDBOX, listOf(twint, legacyIdeal), capabilities, CONTEXT)

        assertEquals(listOf(ProbeRequest(KLARNA_KEY, "klarna.sdk", mapOf("version" to "^2.16.0"))), requests)
    }

    @Test
    fun `checks with each probe's result under its key`() = runTest {
        check(results = mapOf(KLARNA_KEY to ProbeResult(ProbeOutcome.SUCCESS, mapOf("version" to "2.16.0"))))

        val result = JSONObject(results.captured).getJSONObject(KLARNA_KEY)
        assertEquals("success", result.getString("outcome"))
        assertEquals("2.16.0", result.getJSONObject("data").getString("version"))
    }

    @Test
    fun `sends a probe's nested data as JSON`() = runTest {
        val library = mapOf("version" to "2.16.0", "features" to listOf("a", null))
        check(
            results = mapOf(
                KLARNA_KEY to ProbeResult(ProbeOutcome.SUCCESS, mapOf("library" to library, "note" to null)),
            ),
        )

        val data = JSONObject(results.captured).getJSONObject(KLARNA_KEY).getJSONObject("data")
        assertEquals("2.16.0", data.getJSONObject("library").getString("version"))
        assertEquals(2, data.getJSONObject("library").getJSONArray("features").length())
        assertTrue(data.isNull("note"))
    }

    @Test
    fun `checks with no results when no probe ran`() = runTest {
        check()

        assertEquals(0, JSONObject(results.captured).length())
    }

    @Test
    fun `reads each method's verdict by type`() = runTest {
        val verdicts = check()

        assertEquals(ClientRequirementsCheck(satisfied = true, unmet = "[]"), verdicts["TWINT"])
        assertFalse(verdicts.getValue("ADYEN_IDEAL").satisfied)
        assertEquals("url.open", JSONArray(verdicts.getValue("ADYEN_IDEAL").unmet).getJSONObject(0).getString("name"))
    }

    private suspend fun check(results: Map<String, ProbeResult> = emptyMap()) =
        dataSource.check(SANDBOX, listOf(twint, legacyIdeal), capabilities, CONTEXT, results)

    private companion object {
        const val SANDBOX = "sandbox"
        const val CONTEXT = """{"sdk":{"type":"ANDROID_NATIVE"}}"""
        val capabilities = ClientCapabilities(
            stepTypes = mapOf("url.open" to "1.0.0"),
            uiComponents = mapOf("qr.code" to "1.0.0"),
            probes = mapOf("klarna.sdk" to "1.0.0"),
        )
        const val KLARNA_KEY = """klarna.sdk#{"version":"^2.16.0"}"""
        const val KLARNA_PROBE =
            """[{"key": "klarna.sdk#{\"version\":\"^2.16.0\"}", "probe": "klarna.sdk", """ +
                """"params": {"version": "^2.16.0"}}]"""
        const val VERDICTS = """
            {
                "TWINT": {"satisfied": true, "unmet": []},
                "ADYEN_IDEAL": {"satisfied": false, "unmet": [{"kind": "stepType", "name": "url.open"}]}
            }
        """

        val twint = method("pm_twint", "TWINT", PaymentMethodImplementationType.BACKEND_DRIVEN)
        val legacyIdeal = method(
            "pm_ideal",
            "ADYEN_IDEAL",
            PaymentMethodImplementationType.WEB_REDIRECT,
            clientRequirements = """{"stepTypes": {"url.open": "^1.0.0"}}""",
        )

        fun method(
            id: String,
            type: String,
            implementationType: PaymentMethodImplementationType,
            clientRequirements: String? = null,
        ) = PaymentMethodConfigDataResponse(
            id = id,
            name = null,
            implementationType = implementationType,
            type = type,
            options = null,
            displayMetadata = null,
            capabilities = emptySet(),
            clientRequirements = clientRequirements,
        )
    }
}
