package io.primer.android.domain.base

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError

internal abstract class BaseErrorEventResolver(
    private val errorMapperFactory: ErrorMapperFactory,
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter
) {
    protected abstract fun dispatch(error: PrimerError)

    fun resolve(throwable: Throwable, type: ErrorMapperType) {
        val error = errorMapperFactory.buildErrorMapper(type).getPrimerError(throwable)
        logReporter.error(
            "SDK encountered an error: [${error.errorId}] ${error.description}"
        )
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.ERROR,
                error.description,
                Severity.ERROR,
                error.diagnosticsId,
                error.context
            )
        )
        dispatch(error.exposedError)
    }
}
