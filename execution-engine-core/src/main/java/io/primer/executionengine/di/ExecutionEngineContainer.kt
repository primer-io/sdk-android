package io.primer.executionengine.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.executionengine.data.executors.analytics.PlatformLogStepExecutor
import io.primer.executionengine.data.executors.http.HttpStepExecutor
import io.primer.executionengine.data.executors.urlopen.UrlOpenStepExecutor
import io.primer.executionengine.data.handler.DefaultUrlOpenHandler
import io.primer.executionengine.data.registry.DefaultStepExecutorRegistry
import io.primer.executionengine.domain.handler.UrlOpenHandler
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import io.primer.executionengine.domain.registry.StepExecutorRegistry

class ExecutionEngineContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<UrlOpenHandler> {
            DefaultUrlOpenHandler()
        }

        registerSingleton<StepExecutorRegistry> {
            DefaultStepExecutorRegistry(logReporter = sdk().resolve()).apply {
                registerExecutor(
                    HTTP_REQUEST_STEP_TYPE,
                    HttpStepExecutor(
                        httpClient = sdk().resolve(),
                    ),
                )
                registerExecutor(
                    PLATFORM_LOG_STEP_TYPE,
                    PlatformLogStepExecutor(
                        analyticsRepository = sdk().resolve(),
                        logReporter = sdk().resolve(),
                    ),
                )
                registerExecutor(
                    URL_OPEN_STEP_TYPE,
                    UrlOpenStepExecutor(
                        urlOpenHandler = resolve(),
                    ),
                )
                registerExecutor(
                    ANALYTICS_EVENT_STEP_TYPE,
                ) { actionId, _ ->
                    Result.success(
                        StepResult(
                            outcome = Outcome.SUCCESS,
                            actionId = actionId,
                        ),
                    )
                }
            }
        }
    }

    private companion object {

        const val HTTP_REQUEST_STEP_TYPE = "http.request"
        const val PLATFORM_LOG_STEP_TYPE = "platform.log"
        const val URL_OPEN_STEP_TYPE = "url.open"
        const val ANALYTICS_EVENT_STEP_TYPE = "analytics.event"
    }
}
