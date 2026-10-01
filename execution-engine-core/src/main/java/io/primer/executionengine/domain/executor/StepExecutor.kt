package io.primer.executionengine.domain.executor

import io.primer.executionengine.domain.models.StepResult

fun interface StepExecutor {
    /** The version of the step's contract this executor implements. */
    val version: String get() = "1.0.0"

    suspend fun execute(actionId: String, step: String): Result<StepResult>

    /**
     * Invoked once when the checkout flow ends (terminal outcome, error, or cancellation).
     * Implementations must abort in-flight work, be safe to call when idle, and leave the
     * executor reusable (executors are SDK-lifetime singletons).
     */
    fun onFinish() {}
}
