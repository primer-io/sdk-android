package io.primer.executionengine.domain.models

data class StepResult(
    val outcome: Outcome,
    val actionId: String,
    val data: Map<String, Any?> = emptyMap(),
)

enum class Outcome(val value: String) {
    SUCCESS("success"),
    CANCELLED("cancelled"),
    ERROR("error"),
    UNSUPPORTED("unsupported"),
    UNKNOWN("unknown"),
    ;

    companion object {

        fun safeValueOf(value: String?) = Outcome.entries.firstOrNull { it.value == value } ?: UNSUPPORTED
    }
}
