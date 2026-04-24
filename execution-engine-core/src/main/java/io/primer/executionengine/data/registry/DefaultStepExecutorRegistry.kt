package io.primer.executionengine.data.registry

import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import io.primer.executionengine.domain.registry.StepExecutorRegistry

internal class DefaultStepExecutorRegistry(
    private val logReporter: LogReporter,
) : StepExecutorRegistry {
    private val executors = mutableMapOf<String, StepExecutor>()

    override suspend fun executeAction(actionId: String, type: String, params: String): Result<StepResult> =
        runSuspendCatching {
            logReporter.debug("Executing step type='$type' actionId='$actionId'")
            val executor = executors[type]
                ?: return Result.success(
                    StepResult(
                        outcome = Outcome.UNSUPPORTED,
                        actionId = actionId,
                    ),
                )

            return executor.execute(actionId, params).also { result ->
                result.onSuccess {
                    logReporter.debug(
                        "Step type='$type' actionId='$actionId' completed with outcome=${it.outcome}",
                    )
                }
                result.onFailure { logReporter.debug("Step type='$type' actionId='$actionId' failed: ${it.message}") }
            }
        }

    override fun registerExecutor(stepType: String, executor: StepExecutor) {
        executors[stepType] = executor
    }
}
