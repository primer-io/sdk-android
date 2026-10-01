package io.primer.checkout.orchestrator.domain.model

/** The state processor's verdict on a method; [unmet] is the JSON list of what falls short. */
internal data class ClientRequirementsCheck(val satisfied: Boolean, val unmet: String)
