package io.primer.executionengine.data.registry

import io.primer.android.core.extensions.runSuspendCatching
import io.primer.executionengine.domain.probe.Probe
import io.primer.executionengine.domain.probe.ProbeOutcome
import io.primer.executionengine.domain.probe.ProbeResult
import io.primer.executionengine.domain.registry.ProbeRegistry
import java.util.concurrent.ConcurrentHashMap

internal class DefaultProbeRegistry : ProbeRegistry {
    private val probes = ConcurrentHashMap<String, Probe>()

    override val declaredVersions: Map<String, String>
        get() = probes.mapValues { (_, probe) -> probe.version }

    override fun register(name: String, probe: Probe) {
        probes[name] = probe
    }

    override suspend fun run(name: String, params: Map<String, Any?>): ProbeResult =
        probes[name]?.let { probe -> runSuspendCatching { probe.run(params) }.getOrNull() }
            ?: ProbeResult(ProbeOutcome.ERROR)
}
