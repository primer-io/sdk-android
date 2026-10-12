package io.primer.executionengine.domain.registry

import io.primer.executionengine.domain.probe.Probe
import io.primer.executionengine.domain.probe.ProbeResult

/** The probes this SDK can run, by name. */
interface ProbeRegistry {
    val declaredVersions: Map<String, String>

    fun register(name: String, probe: Probe)

    /** Runs probe [name]; a missing or failing probe answers [ProbeOutcome.ERROR]. */
    suspend fun run(name: String, params: Map<String, Any?>): ProbeResult
}
