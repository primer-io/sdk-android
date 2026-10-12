package io.primer.executionengine.data.registry

import io.primer.executionengine.domain.probe.Probe
import io.primer.executionengine.domain.probe.ProbeOutcome
import io.primer.executionengine.domain.probe.ProbeResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DefaultProbeRegistryTest {
    private val registry = DefaultProbeRegistry()

    @Test
    fun `declares nothing until a module registers`() {
        assertTrue(registry.declaredVersions.isEmpty())
    }

    @Test
    fun `declares each probe at its version, the last registration winning`() {
        registry.register("klarna.sdk", found("2.11.3"))
        registry.register(
            "googlePay.isReadyToPay",
            object : Probe {
                override val version = "2.0.0"

                override suspend fun run(params: Map<String, Any?>) = ProbeResult(ProbeOutcome.FAILURE)
            },
        )
        registry.register("klarna.sdk", found("2.16.0"))

        assertEquals(mapOf("klarna.sdk" to "1.0.0", "googlePay.isReadyToPay" to "2.0.0"), registry.declaredVersions)
    }

    @Test
    fun `runs a probe with the check's params`() = runTest {
        var received: Map<String, Any?>? = null
        registry.register("klarna.sdk") { params ->
            received = params
            ProbeResult(ProbeOutcome.SUCCESS, mapOf("version" to "2.16.0"))
        }

        val result = registry.run("klarna.sdk", mapOf("version" to "^2.16.0"))

        assertEquals(ProbeResult(ProbeOutcome.SUCCESS, mapOf("version" to "2.16.0")), result)
        assertEquals(mapOf("version" to "^2.16.0"), received)
    }

    @Test
    fun `answers error for a probe that is not there, or throws`() = runTest {
        registry.register("klarna.sdk") { error("no SDK") }

        assertEquals(ProbeOutcome.ERROR, registry.run("klarna.sdk", emptyMap()).outcome)
        assertEquals(ProbeOutcome.ERROR, registry.run("applePay.canMakePayments", emptyMap()).outcome)
    }

    private fun found(version: String) = Probe { ProbeResult(ProbeOutcome.SUCCESS, mapOf("version" to version)) }
}
