package io.primer.android.errors.domain

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.models.PrimerError

/**
 * Maps exceptions to [PrimerError] instances, while logging them to analytics.
 */
abstract class BaseErrorResolver(
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter
) {
    /**
     * Maps the given [Throwable] to a [PrimerError] and logs the error to analytics.
     */
    fun resolve(throwable: Throwable): PrimerError {
        val error = errorMapperRegistry.getPrimerError(throwable)
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

        return error.exposedError
    }
}
