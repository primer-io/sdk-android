package io.primer.executionengine.domain.executor

import io.primer.executionengine.domain.models.StepResult

fun interface StepExecutor {
    suspend fun execute(actionId: String, step: String): Result<StepResult>
}
