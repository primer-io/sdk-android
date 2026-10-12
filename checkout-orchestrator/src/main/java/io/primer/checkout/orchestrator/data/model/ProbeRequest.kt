package io.primer.checkout.orchestrator.data.model

/** A probe the client requirements check needs run; its result goes back under [key]. */
internal data class ProbeRequest(
    val key: String,
    val probe: String,
    val params: Map<String, Any?>,
)
