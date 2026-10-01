package io.primer.executionengine.domain.registry

import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.StepResult

interface StepExecutorRegistry {
    /** Each registered step type and its version, as client requirements are checked against. */
    val declaredVersions: Map<String, String>

    suspend fun executeAction(actionId: String, type: String, params: String): Result<StepResult>
    fun registerExecutor(stepType: String, executor: StepExecutor)
    fun onFinish()
}
