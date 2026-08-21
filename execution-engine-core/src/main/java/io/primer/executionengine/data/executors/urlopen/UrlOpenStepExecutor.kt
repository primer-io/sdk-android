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
        when (event.value) {
            VALUE_CANCELLED -> StepResult(
                outcome = Outcome.CANCELLED,
                actionId = actionId,
                data = event.data,
            )

            else -> StepResult(
                outcome = Outcome.ERROR,
                actionId = actionId,
            )
        }
    }

    private companion object {
        const val VALUE_CANCELLED = "cancelled"
    }
}
