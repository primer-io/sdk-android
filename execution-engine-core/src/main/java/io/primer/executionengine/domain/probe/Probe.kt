package io.primer.executionengine.domain.probe

/** A device check a payment method's client requirements can ask for, such as whether a library is present. */
fun interface Probe {
    /** The version of the probe contract this implements. */
    val version: String get() = "1.0.0"

    suspend fun run(params: Map<String, Any?>): ProbeResult
}

/** A probe's outcome, and what it found, such as a library's `version`. */
data class ProbeResult(
    val outcome: ProbeOutcome,
    val data: Map<String, Any?> = emptyMap(),
)

/** Only [SUCCESS] meets the check. */
enum class ProbeOutcome(val value: String) {
    SUCCESS("success"),
    FAILURE("failure"),
    ERROR("error"),
}
