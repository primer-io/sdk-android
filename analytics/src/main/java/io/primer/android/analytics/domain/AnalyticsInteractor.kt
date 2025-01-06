package io.primer.android.analytics.domain

import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsInteractor(
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Unit, BaseAnalyticsParams>() {
    suspend fun startObservingEvents() =
        withContext(dispatcher) {
            analyticsRepository.startObservingEvents()
        }

    override suspend fun performAction(params: BaseAnalyticsParams): Result<Unit> =
        runSuspendCatching {
            analyticsRepository.addEvent(params)
        }.onFailure {
            logReporter.warn(ANALYTICS_ERROR)
        }

    fun send() = analyticsRepository.send()

    private companion object {
        const val ANALYTICS_ERROR = "Failed to add analytics events."
    }
}
