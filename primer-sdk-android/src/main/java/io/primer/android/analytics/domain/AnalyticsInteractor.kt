package io.primer.android.analytics.domain

import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

internal class AnalyticsInteractor(
    private val analyticsRepository: AnalyticsRepository,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<Boolean, BaseAnalyticsParams>() {

    fun initialize() = analyticsRepository.initialize().flowOn(dispatcher)

    override fun execute(params: BaseAnalyticsParams) =
        analyticsRepository.addEvent(params).catch { logger.warn(ANALYTICS_ERROR) }

    fun send() = analyticsRepository.send()

    private companion object {
        const val ANALYTICS_ERROR = "Failed to add analytics events."
    }
}
