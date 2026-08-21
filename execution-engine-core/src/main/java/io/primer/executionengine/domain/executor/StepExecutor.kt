package io.primer.executionengine.domain.executor

import io.primer.executionengine.domain.models.StepResult

fun interface StepExecutor {
    suspend fun execute(actionId: String, step: String): Result<StepResult>

    /**
     * Invoked once when the checkout flow ends (terminal outcome, error, or cancellation).
     * Implementations must abort in-flight work, be safe to call when idle, and leave the
     * executor reusable (executors are SDK-lifetime singletons).
     */
    fun onFinish() {}
}
