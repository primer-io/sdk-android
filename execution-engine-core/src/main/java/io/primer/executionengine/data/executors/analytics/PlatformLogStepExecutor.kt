package io.primer.executionengine.data.executors.analytics

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult

internal class PlatformLogStepExecutor(
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter,
) : StepExecutor {

    override suspend fun execute(actionId: String, step: String): Result<StepResult> {
        runSuspendCatching { analyticsRepository.addRawEvent(step) }
            .onFailure { logReporter.debug("Failed to log analytics event: ${it.message}") }
        return Result.success(
            StepResult(outcome = Outcome.SUCCESS, actionId = actionId),
        )
    }
}
