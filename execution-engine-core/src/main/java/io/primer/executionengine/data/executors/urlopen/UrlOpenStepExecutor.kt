package io.primer.executionengine.data.executors.urlopen

import io.primer.android.core.extensions.runSuspendCatching
import io.primer.executionengine.data.models.urlopen.UrlOpenParams
import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.handler.UrlOpenHandler
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import kotlinx.coroutines.flow.first
import org.json.JSONObject

internal class UrlOpenStepExecutor(
    private val urlOpenHandler: UrlOpenHandler,
) : StepExecutor {

    override suspend fun execute(actionId: String, step: String): Result<StepResult> = runSuspendCatching {
        val params = UrlOpenParams.deserializer.deserialize(JSONObject(step))

        urlOpenHandler.launch(params.url, params.redirectUrls)

        val event = urlOpenHandler.componentEvents.first()
        val outcome = when (event.value) {
            VALUE_SUCCESS -> Outcome.SUCCESS
            VALUE_CANCELLED -> Outcome.CANCELLED
            else -> Outcome.ERROR
        }

        StepResult(
            outcome = outcome,
            actionId = actionId,
        )
    }

    private companion object {
        const val VALUE_SUCCESS = "completed"
        const val VALUE_CANCELLED = "cancelled"
    }
}
