package io.primer.checkout.orchestrator.data

import io.primer.checkout.orchestrator.data.model.ClientCapabilities
import io.primer.executionengine.domain.registry.ProbeRegistry
import io.primer.executionengine.domain.registry.StepExecutorRegistry
import io.primer.executionengine.domain.registry.UiComponentRegistry

/** What this SDK declares to the client requirements check. */
internal class ClientCapabilitiesProvider(
    private val stepExecutorRegistry: StepExecutorRegistry,
    private val uiComponentRegistry: UiComponentRegistry,
    private val probeRegistry: ProbeRegistry,
) {
    fun provide() = ClientCapabilities(
        stepTypes = stepExecutorRegistry.declaredVersions,
        uiComponents = uiComponentRegistry.declaredVersions,
        probes = probeRegistry.declaredVersions,
    )
}
