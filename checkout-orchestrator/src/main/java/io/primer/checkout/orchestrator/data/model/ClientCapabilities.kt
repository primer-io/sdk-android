package io.primer.checkout.orchestrator.data.model

/** What this SDK implements, as versions by name. */
internal data class ClientCapabilities(
    val stepTypes: Map<String, String>,
    val uiComponents: Map<String, String>,
    val probes: Map<String, String>,
)
