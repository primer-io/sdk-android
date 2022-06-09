package io.primer.android.domain.base

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError

internal abstract class BaseErrorEventResolver(
    private val errorMapperFactory: ErrorMapperFactory,
    private val analyticsRepository: AnalyticsRepository
) {
    protected abstract fun dispatch(error: PrimerError)

    fun resolve(throwable: Throwable, type: ErrorMapperType) {
        val error = errorMapperFactory.buildErrorMapper(type).getPrimerError(throwable)
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.ERROR,
                error.description,
                Severity.ERROR,
                error.diagnosticsId
            )
        )
        dispatch(error.exposedError)
    }
}
