package io.primer.executionengine.domain.registry

import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.StepResult

interface StepExecutorRegistry {
    suspend fun executeAction(actionId: String, type: String, params: String): Result<StepResult>
    fun registerExecutor(stepType: String, executor: StepExecutor)
    fun onFinish()
}
